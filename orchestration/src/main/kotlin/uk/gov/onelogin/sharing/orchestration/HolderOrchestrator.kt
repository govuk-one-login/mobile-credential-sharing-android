package uk.gov.onelogin.sharing.orchestration

import androidx.annotation.Keep
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPrivateKey
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateException
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.DecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.cryptoService.holder.DeviceSignatureException
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANNOT_TRANSITION_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.completedPrerequisiteChecks
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.createSessionResetMessage
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.recreateSessionOnStartMessage
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestException
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandler
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.ConfirmConsentUseCase
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionContext
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteGate

@Keep
@Suppress("LongParameterList", "TooManyFunctions")
@SingleIn(AppScope::class)
@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Holder>())
class HolderOrchestrator(
    private val logger: Logger,
    private val sessionFactory: SessionFactory<HolderSession>,
    private val peripheralBluetoothTransport: PeripheralBluetoothTransport,
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope,
    private val decryptDeviceRequestUseCase: DecryptDeviceRequestUseCase,
    private val holderCryptoService: HolderCryptoService,
    private val prerequisiteGate: PrerequisiteGate,
    private val confirmConsentUseCase: ConfirmConsentUseCase,
    private val credentialRequestHandler: CredentialRequestHandler
) : Orchestrator.Holder {
    private var transportStateJob: Job? = null
    private val sessionFlow = MutableStateFlow(sessionFactory.create())
    private val currentContext: HolderSessionContext get() = sessionFlow.value.sessionContext

    @OptIn(ExperimentalCoroutinesApi::class)
    override val holderSessionState: StateFlow<HolderSessionState> = sessionFlow.flatMapLatest {
        it.currentState
    }.stateIn(
        appCoroutineScope,
        SharingStarted.Eagerly,
        sessionFlow.value.currentState.value
    )

    init {
        transportStateJob = appCoroutineScope.launch {
            peripheralBluetoothTransport.state.collect {
                handleMdocState(it)
            }
        }
    }

    override fun start() {
        if (sessionFlow.value.isComplete()) {
            sessionFlow.update {
                sessionFactory.create().also {
                    logger.debug(
                        logTag,
                        recreateSessionOnStartMessage(Orchestrator.Holder.JOURNEY_NAME)
                    )
                }
            }
        }

        if (sessionFlow.value.currentState.value !is HolderSessionState.NotStarted) {
            logger.error(
                logTag,
                START_ORCHESTRATION_ERROR,
                OrchestratorCannotStartException(
                    START_ORCHESTRATION_ERROR,
                    IllegalStateException("Journey already in progress")
                )
            )
            return
        }

        performPreflightChecks()
    }

    private fun performPreflightChecks() {
        try {
            prerequisiteGate.evaluatePrerequisites(
                Prerequisite.BLUETOOTH
            ).also {
                logger.debug(
                    logTag,
                    completedPrerequisiteChecks(
                        journey = Orchestrator.Holder.JOURNEY_NAME,
                        response = it
                    )
                )
            }.let { prerequisiteCheck ->
                handleStartPrerequisiteCheck(prerequisiteCheck)
                logger.debug(logTag, START_ORCHESTRATION_SUCCESS)
            }
        } catch (exception: IllegalStateException) {
            START_ORCHESTRATION_ERROR.let { logMessage ->
                logger.error(
                    logTag,
                    logMessage,
                    OrchestratorCannotStartException(logMessage, exception)
                )
            }
        }
    }

    private fun handleStartPrerequisiteCheck(prerequisiteCheck: List<MissingPrerequisite>) {
        if (prerequisiteCheck.isEmpty()) {
            safeTransitionTo(HolderSessionState.ReadyToPresent)

            appCoroutineScope.launch {
                peripheralBluetoothTransport.start(
                    serviceUuid = currentContext.sessionUuid
                )
            }

            val qrCode = currentContext.qrCode
            if (qrCode.isNotEmpty()) {
                safeTransitionTo(HolderSessionState.PresentingEngagement(qrCode))
            }
        } else {
            val checkResponse = prerequisiteCheck[0]

            when {
                !checkResponse.isRecoverable() -> {
                    HolderSessionState.Complete.Failed(
                        SessionError(
                            "Device cannot perform journey",
                            SessionErrorReason.UnrecoverablePrerequisite(checkResponse)
                        )
                    )
                }

                else ->
                    HolderSessionState.Preflight(
                        missingPrerequisites = prerequisiteCheck,
                        onComplete = ::performPreflightChecks
                    )
            }.let(::safeTransitionTo)
        }
    }

    override fun confirmConsent() {
        val state = holderSessionState.value
        val context = currentContext
        try {
            assert(state is HolderSessionState.AwaitingUserConsent) {
                "confirmConsent called in an invalid state: $state"
            }
            check(state is HolderSessionState.AwaitingUserConsent)
            safeTransitionTo(HolderSessionState.ProcessingResponse)

            val sessionTranscript = checkNotNull(context.sessionTranscriptBytes) {
                "Missing session transcript"
            }
            val validatedCredential = checkNotNull(context.validatedCredential) {
                "Missing validated credential"
            }
            val filteredIssuerSigned = checkNotNull(context.filteredIssuerSigned) {
                "Missing filtered issuer signed"
            }

            val skDevice = checkNotNull(context.skDevice) {
                "Missing skDevice"
            }

            appCoroutineScope.launch {
                try {
                    val document = confirmConsentUseCase.execute(
                        sessionTranscript = sessionTranscript,
                        deviceRequest = state.request,
                        validatedCredential = validatedCredential,
                        filteredIssuerSigned = filteredIssuerSigned
                    )

                    val sessionDataBytes = holderCryptoService.buildDeviceResponse(
                        documents = listOf(document),
                        skDevice = skDevice,
                        encryptCounter = context.encryptCounter
                    )

                    val sent = peripheralBluetoothTransport.sendMessage(
                        serviceUuid = context.sessionUuid,
                        data = sessionDataBytes
                    )

                    sessionFlow.value.updateSessionContext {
                        it.copy(encryptCounter = it.encryptCounter + 1u)
                    }

                    if (sent) {
                        safeTransitionTo(HolderSessionState.AwaitingVerifierResolution)
                    }
                } catch (e: DeviceSignatureException) {
                    sendTerminationAndFail(e)
                }
            }
        } catch (e: IllegalStateException) {
            sendTerminationAndFail(e)
        }
    }

    override fun denyConsent() {
        val state = holderSessionState.value
        val context = currentContext
        try {
            assert(state is HolderSessionState.AwaitingUserConsent) {
                "denyConsent called in an invalid state: $state"
            }
            check(state is HolderSessionState.AwaitingUserConsent)
            safeTransitionTo(HolderSessionState.ProcessingResponse)

            val skDevice = checkNotNull(context.skDevice) {
                "Missing skDevice"
            }

            val sessionDataBytes = holderCryptoService.buildErrorSessionData(
                deviceResponseStatus = Status.OK,
                sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
                skDevice = skDevice,
                encryptCounter = context.encryptCounter
            )

            appCoroutineScope.launch {
                val sent = peripheralBluetoothTransport.sendMessage(
                    serviceUuid = context.sessionUuid,
                    data = sessionDataBytes
                )

                if (sent) {
                    delay(TERMINATION_DELAY.milliseconds)
                    peripheralBluetoothTransport.notifySessionEnd(context.sessionUuid)
                }

                safeTransitionTo(
                    HolderSessionState.Complete.Success(
                        HolderSessionState.Complete.SuccessReason.Denied
                    )
                )
            }
        } catch (e: IllegalStateException) {
            sendTerminationAndFail(e)
        }
    }

    override fun cancel() {
        safeTransitionTo(
            state = HolderSessionState.Complete.Cancelled,
            exceptionWrapper = ::OrchestratorCannotCancelException
        )

        stopAdvertising(sendEndCommand = true)
    }

    override fun reset() {
        sessionFlow.update {
            sessionFactory.create().also {
                logger.debug(
                    logTag,
                    createSessionResetMessage(Orchestrator.Holder.JOURNEY_NAME)
                )
            }
        }
    }

    private fun stopAdvertising(sendEndCommand: Boolean) {
        appCoroutineScope.launch {
            peripheralBluetoothTransport.stop(
                serviceUuid = currentContext.sessionUuid,
                sendEndCommand = sendEndCommand
            )
        }
    }

    @Suppress("LongMethod")
    private fun handleMdocState(state: PeripheralBluetoothState) {
        logger.debug(logTag, "state = $state")

        if (sessionFlow.value.isComplete()) {
            logger.debug(logTag, "Session already complete, ignoring BLE state")
            return
        }

        when (state) {
            is PeripheralBluetoothState.Connected -> {
                safeTransitionTo(HolderSessionState.ProcessingEstablishment)

                logger.debug(logTag, "Mdoc - Connected: ${state.address}")
            }

            is PeripheralBluetoothState.Disconnected -> {
                @RequiresImplementation(
                    details = [
                        ImplementationDetail(
                            ticket = "DCMAW-16898",
                            description = "We may need to handle explicit bluetooth " +
                                "disconnection states to handle common error codes " +
                                "8, 19, 22 and 133. The function below will handle " +
                                "treat all disconnect states the same when connected " +
                                "to a device"
                        )
                    ]
                )

                if (state.isSessionEnd) {
                    logger.debug(
                        logTag,
                        "BLE session terminated successfully via GATT End command"
                    )
                    stopAdvertising(sendEndCommand = false)
                } else {
                    logger.debug(logTag, "Error Mdoc - Disconnected: ${state.address}")

                    val message = "Device ${state.address} disconnected unexpectedly"
                    failWith(
                        message = message,
                        reason = SessionErrorReason.InvalidBluetoothState(
                            BluetoothDisconnectedException(
                                "Bluetooth disconnected unexpectedly",
                                IllegalStateException(
                                    "Device ${state.address} disconnected unexpectedly"
                                )
                            )
                        )
                    )
                }
            }

            is PeripheralBluetoothState.Error -> {
                failWith(
                    "Mdoc - Error: ${state.reason.message}",
                    SessionErrorReason.InvalidBluetoothState(
                        PeripheralBluetoothStateException(state.reason)
                    )
                )
            }

            PeripheralBluetoothState.Idle -> Unit

            is PeripheralBluetoothState.Ended -> {
                when {
                    sessionFlow.value.currentState.value is
                        HolderSessionState.ProcessingResponse -> Unit

                    sessionFlow.value.currentState.value is
                        HolderSessionState.AwaitingVerifierResolution &&
                        state.status == SessionEndStates.SUCCESS -> {
                        safeTransitionTo(HolderSessionState.Complete.Success())
                    }

                    else -> {
                        safeTransitionTo(HolderSessionState.Complete.Cancelled)
                    }
                }

                if (state.status == SessionEndStates.SUCCESS) {
                    logger.debug(logTag, "Mdoc - Ending session")
                } else {
                    logger.error(
                        logTag,
                        "Mdoc - Error while ending session: ${state.status}"
                    )
                }
            }

            is PeripheralBluetoothState.MessageReceived -> {
                handleMessageReceived(state.message)
            }
        }
    }

    private fun handleMessageReceived(message: ByteArray) {
        val keypair = currentContext.keyPair?.private
        if (keypair !is ECPrivateKey) {
            sendTerminationAndFail(IllegalStateException("Invalid or missing keypair"))
            return
        }

        try {
            val deviceRequest = decryptDeviceRequestUseCase.execute(
                sessionEstablishmentBytes = message,
                engagement = currentContext.engagement,
                holderPrivateKey = keypair,
                decryptCounter = currentContext.decryptCounter,
                onDeriveSkDevice = { skDevice ->
                    sessionFlow.value.updateSessionContext {
                        it.copy(skDevice = skDevice)
                    }
                },
                onDeriveSessionTranscript = { transcript ->
                    sessionFlow.value.updateSessionContext {
                        it.copy(sessionTranscriptBytes = transcript)
                    }
                }
            )

            sessionFlow.value.updateSessionContext {
                it.copy(decryptCounter = it.decryptCounter + 1u)
            }

            val requestedDocType = deviceRequest.docRequests.first().itemsRequest.docType
            appCoroutineScope.launch {
                requestAndValidateCredential(requestedDocType, deviceRequest)
            }
        } catch (e: DeviceRequestDecodingException) {
            handleDeviceRequestFailure(e)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            sendTerminationAndFail(e)
        }
    }

    private suspend fun requestAndValidateCredential(
        requestedDocType: String,
        deviceRequest: DeviceRequest
    ) {
        try {
            val result = credentialRequestHandler.requestAndValidate(
                requestedDocType,
                deviceRequest
            )

            sessionFlow.value.updateSessionContext {
                it.copy(
                    validatedCredential = result.validatedCredential,
                    filteredIssuerSigned = result.filteredIssuerSigned
                )
            }

            logger.debug(logTag, CredentialRequestHandlerImpl.LOG_DOCTYPE_MATCH)
            safeTransitionTo(HolderSessionState.AwaitingUserConsent(deviceRequest))
        } catch (e: CredentialRequestException) {
            handleNoMatchTermination(e)
        }
    }

    private suspend fun handleNoMatchTermination(exception: Exception) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        val context = currentContext
        val skDevice = context.skDevice

        if (skDevice != null) {
            val sessionDataBytes = holderCryptoService.buildErrorSessionData(
                deviceResponseStatus = Status.OK,
                sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
                skDevice = skDevice,
                encryptCounter = context.encryptCounter
            )

            val sent = peripheralBluetoothTransport.sendMessage(
                serviceUuid = context.sessionUuid,
                data = sessionDataBytes
            )

            safeTransitionTo(
                HolderSessionState.Complete.Success(
                    HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
                )
            )

            if (sent) {
                delay(TERMINATION_DELAY.milliseconds)
                peripheralBluetoothTransport.notifySessionEnd(context.sessionUuid)
            }
        } else {
            sendTerminationAndFail(
                IllegalStateException("Missing skDevice during no-match termination")
            )
        }
    }

    private fun handleDeviceRequestFailure(exception: DeviceRequestDecodingException) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        val context = currentContext
        val skDevice = checkNotNull(context.skDevice) {
            "skDevice must be derived before handling DeviceRequest failure"
        }

        holderCryptoService.buildErrorSessionData(
            deviceResponseStatus = Status.CBOR_DECODING_ERROR,
            sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
            skDevice = skDevice,
            encryptCounter = context.encryptCounter
        )

        safeTransitionTo(
            HolderSessionState.Complete.Failed(
                SessionError(
                    message = exception.message ?: UNKNOWN_ERROR,
                    exception = exception
                )
            )
        )
    }

    private fun sendTerminationAndFail(exception: Exception) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        holderCryptoService.buildTerminationSessionData(SessionDataStatus.SESSION_TERMINATION)
        safeTransitionTo(
            HolderSessionState.Complete.Failed(
                SessionError(
                    message = exception.message ?: UNKNOWN_ERROR,
                    exception = exception
                )
            )
        )
    }

    private fun failWith(
        message: String,
        reason: SessionErrorReason,
        sendEndCommand: Boolean = true
    ) {
        logger.error(logTag, message)
        stopAdvertising(sendEndCommand)
        safeTransitionTo(
            HolderSessionState.Complete.Failed(
                SessionError(message = message, reason = reason)
            )
        )
    }

    private fun failWith(
        message: String,
        error: SessionError,
        throwable: Throwable,
        sendEndCommand: Boolean = true
    ) {
        logger.error(logTag, message, throwable)
        stopAdvertising(sendEndCommand)
        safeTransitionTo(
            HolderSessionState.Complete.Failed(error)
        )
    }

    private fun safeTransitionTo(
        state: HolderSessionState,
        logMessage: String = CANNOT_TRANSITION_TO_STATE.format(
            sessionFlow.value.currentState.value,
            state
        ),
        exceptionWrapper: ((String, Throwable) -> Exception)? = null
    ) {
        try {
            sessionFlow.value.transitionTo(state)
            logger.debug(logTag, "$TRANSITION_SUCCESSFUL_TO_STATE $state")
        } catch (exception: IllegalStateException) {
            val loggedException = exceptionWrapper?.invoke(logMessage, exception) ?: exception
            logger.error(logTag, logMessage, loggedException)
        }
    }

    private companion object {
        const val UNKNOWN_ERROR = "Unknown error"
        const val TERMINATION_DELAY = 500L
    }
}
