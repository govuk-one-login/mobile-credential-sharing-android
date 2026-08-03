package uk.gov.onelogin.sharing.orchestration

import androidx.annotation.Keep
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPrivateKey
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import uk.gov.onelogin.sharing.core.coroutines.CoroutineNameExt.asCoroutineName
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.sessionTimer.SessionTimer
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestValidationException
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
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestException
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandler
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.ConfirmConsentUseCase
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionContext
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionTerminator
import uk.gov.onelogin.sharing.orchestration.holder.session.InboundMessageClassifier
import uk.gov.onelogin.sharing.orchestration.holder.session.InboundMessageType
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteGate

@Keep
@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
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
    private val credentialRequestHandler: CredentialRequestHandler,
    private val holderSessionTerminator: HolderSessionTerminator,
    private val inboundMessageClassifier: InboundMessageClassifier,
    private val sessionTimer: SessionTimer
) : Orchestrator.Holder {
    internal var transportStateJob: Job? = null
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

    internal fun handleStartPrerequisiteCheck(prerequisiteCheck: List<MissingPrerequisite>) {
        if (prerequisiteCheck.isEmpty()) {
            safeTransitionTo(HolderSessionState.ReadyToPresent)

            monitorBluetoothTransportState()

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

    internal fun monitorBluetoothTransportState() {
        transportStateJob?.let(Job::cancel)

        transportStateJob = appCoroutineScope.launch(
            "$logTag.transportStateJob".asCoroutineName()
        ) {
            peripheralBluetoothTransport.state.collect {
                handleMdocState(it)
            }
        }

        logger.debug(
            logTag,
            "Started bluetooth transport state monitoring"
        )
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

            val skDevice = checkNotNull(context.skDevice) { "Missing skDevice" }

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
                    sessionTimer.reset()

                    sessionFlow.value.updateSessionContext {
                        it.copy(encryptCounter = it.encryptCounter + 1u)
                    }

                    if (sent) {
                        safeTransitionTo(HolderSessionState.AwaitingVerifierResolution)
                    } else {
                        failWith(
                            message = "Failed to send DeviceResponse",
                            reason = SessionErrorReason.CannotSendMessage
                        )
                    }
                } catch (e: DeviceSignatureException) {
                    sendTerminationAndFail(e)
                }
            }
        } catch (e: IllegalStateException) {
            appCoroutineScope.launch {
                sendTerminationAndFail(e)
            }
        }
    }

    override fun denyConsent() {
        val state = holderSessionState.value
        val context = currentContext
        try {
            check(state is HolderSessionState.AwaitingUserConsent) {
                "denyConsent called in an invalid state: $state"
            }

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
                terminateSession(
                    finalState = HolderSessionState.Complete.Success(
                        HolderSessionState.Complete.SuccessReason.Denied
                    ),
                    sessionDataToSend = sessionDataBytes
                )
            }
        } catch (e: IllegalStateException) {
            appCoroutineScope.launch {
                sendTerminationAndFail(e)
            }
        }
    }

    override fun cancel() {
        if (sessionFlow.value.isComplete()) return
        appCoroutineScope.launch {
            terminateSession(
                finalState = HolderSessionState.Complete.Cancelled,
                sessionDataToSend = null,
                sendEndCommand = true
            )
        }
    }

    override fun reset() {
        stopBluetoothTransportStateMonitoring()

        sessionFlow.update {
            sessionFactory.create().also {
                logger.debug(
                    logTag,
                    createSessionResetMessage(Orchestrator.Holder.JOURNEY_NAME)
                )
            }
        }
    }

    private fun stopBluetoothTransportStateMonitoring() {
        transportStateJob?.cancel()
        transportStateJob = null
        logger.debug(
            logTag,
            "Stopped bluetooth transport state monitoring"
        )
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

        val currentState = sessionFlow.value.currentState.value
        when {
            currentState.isComplete() -> "Session already complete, ignoring BLE state"

            currentState is HolderSessionState.SendingTermination ->
                "Session complete or terminating, ignoring BLE state"

            currentState is HolderSessionState.NotStarted ->
                "Session not started, ignoring BLE state"

            else -> null
        }?.let { logMessage ->
            logger.debug(logTag, logMessage)
            return
        }

        when (state) {
            is PeripheralBluetoothState.Connected -> {
                sessionTimer.start(INACTIVITY_TIMEOUT) { cancel() }

                safeTransitionTo(HolderSessionState.ProcessingEstablishment)
                logger.debug(logTag, "Mdoc - Connected: ${state.address}")
            }

            is PeripheralBluetoothState.Disconnected -> {
                if (state.isSessionEnd) {
                    logger.debug(logTag, "BLE session terminated successfully via GATT End command")
                    stopAdvertising(sendEndCommand = false)
                } else {
                    handleConnectionLoss(state.address)
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

            is PeripheralBluetoothState.Ended -> handleSessionEnded(state)

            is PeripheralBluetoothState.MessageReceived -> handleMessageReceived(state.message)
        }
    }

    private fun handleMessageReceived(message: ByteArray) {
        sessionTimer.reset()
        when (val type = inboundMessageClassifier.getMessageType(message)) {
            is InboundMessageType.SessionEstablishment ->
                handleSessionEstablishment(message)

            is InboundMessageType.StatusOnly ->
                handlePeerTermination(type.status)

            is InboundMessageType.Unknown -> {
                logger.error(logTag, UNRECOGNISED_MESSAGE)
                appCoroutineScope.launch {
                    sendTerminationAndFail(
                        IllegalStateException(UNRECOGNISED_MESSAGE)
                    )
                }
            }
        }
    }

    private fun handleSessionEnded(state: PeripheralBluetoothState.Ended) {
        if (state.status == SessionEndStates.SUCCESS) {
            logger.debug(logTag, "Mdoc - Ending session")
        } else {
            logger.error(logTag, "Mdoc - Error while ending session: ${state.status}")
        }

        handleConnectionLoss(isGattEnd = true)
    }

    private fun handleSessionEstablishment(message: ByteArray) {
        val keypair = validateSessionEstablishmentPreconditions() ?: return

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

            if (!deviceRequestContainsPortrait(deviceRequest)) {
                logger.error(logTag, PORTRAIT_POLICY_VIOLATION)
                appCoroutineScope.launch {
                    handlePolicyViolation()
                }
                return
            }

            val requestedDocType = deviceRequest.docRequests.first().itemsRequest.docType
            appCoroutineScope.launch {
                requestAndValidateCredential(requestedDocType, deviceRequest)
            }
        } catch (e: DeviceRequestValidationException) {
            appCoroutineScope.launch {
                handleDeviceRequestValidationFailure(e)
            }
        } catch (e: DeviceRequestDecodingException) {
            appCoroutineScope.launch {
                handleDeviceRequestFailure(e)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            appCoroutineScope.launch {
                sendTerminationAndFail(e)
            }
        }
    }

    /**
     * Validates that the session is in the correct state and has a valid keypair.
     * Returns the [ECPrivateKey] if preconditions are met, or `null` if termination was triggered.
     */
    private fun validateSessionEstablishmentPreconditions(): ECPrivateKey? {
        val currentState = holderSessionState.value

        if (currentState !is HolderSessionState.ProcessingEstablishment) {
            logger.error(
                logTag,
                "Sequencing violation: message received in state $currentState"
            )
            appCoroutineScope.launch {
                sendTerminationAndFail(
                    IllegalStateException(
                        "Sequencing violation: message received in state $currentState"
                    )
                )
            }
            return null
        }

        return (currentContext.keyPair?.private as? ECPrivateKey).also { keypair ->
            if (keypair == null) {
                appCoroutineScope.launch {
                    sendTerminationAndFail(
                        IllegalStateException("Invalid or missing keypair")
                    )
                }
            }
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

            terminateSession(
                finalState = HolderSessionState.Complete.Success(
                    HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
                ),
                sessionDataToSend = sessionDataBytes
            )
        } else {
            sendTerminationAndFail(
                IllegalStateException("Missing skDevice during no-match termination")
            )
        }
    }

    private suspend fun handleDeviceRequestFailure(exception: DeviceRequestDecodingException) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        val context = currentContext
        val skDevice = checkNotNull(context.skDevice) {
            "skDevice must be derived before handling DeviceRequest failure"
        }

        val sessionDataBytes = holderCryptoService.buildErrorSessionData(
            deviceResponseStatus = Status.CBOR_DECODING_ERROR,
            sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
            skDevice = skDevice,
            encryptCounter = context.encryptCounter
        )

        terminateSession(
            finalState = HolderSessionState.Complete.Failed(
                SessionError(message = exception.message ?: UNKNOWN_ERROR, exception = exception)
            ),
            sessionDataToSend = sessionDataBytes
        )
    }

    private suspend fun handleDeviceRequestValidationFailure(
        exception: DeviceRequestValidationException
    ) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        val context = currentContext
        val skDevice = checkNotNull(context.skDevice) {
            "skDevice must be derived before handling DeviceRequest validation failure"
        }

        val sessionDataBytes = holderCryptoService.buildErrorSessionData(
            deviceResponseStatus = Status.CBOR_VALIDATION_ERROR,
            sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
            skDevice = skDevice,
            encryptCounter = context.encryptCounter
        )

        terminateSession(
            finalState = HolderSessionState.Complete.Failed(
                SessionError(message = exception.message ?: UNKNOWN_ERROR, exception = exception)
            ),
            sessionDataToSend = sessionDataBytes
        )
    }

    private suspend fun handlePolicyViolation() {
        val context = currentContext
        val skDevice = checkNotNull(context.skDevice) {
            "skDevice must be derived before handling policy violation"
        }

        val sessionDataBytes = holderCryptoService.buildErrorSessionData(
            deviceResponseStatus = Status.GENERAL_ERROR,
            sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
            skDevice = skDevice,
            encryptCounter = context.encryptCounter
        )

        terminateSession(
            finalState = HolderSessionState.Complete.Failed(
                SessionError(
                    message = PORTRAIT_POLICY_VIOLATION,
                    exception = IllegalStateException(PORTRAIT_POLICY_VIOLATION)
                )
            ),
            sessionDataToSend = sessionDataBytes
        )
    }

    private suspend fun sendTerminationAndFail(exception: Exception) {
        logger.error(logTag, exception.message ?: UNKNOWN_ERROR, exception)
        val sessionDataBytes = holderCryptoService.buildTerminationSessionData(
            SessionDataStatus.SESSION_TERMINATION
        )

        terminateSession(
            finalState = HolderSessionState.Complete.Failed(
                SessionError(message = exception.message ?: UNKNOWN_ERROR, exception = exception)
            ),
            sessionDataToSend = sessionDataBytes
        )
    }

    private fun failWith(message: String, reason: SessionErrorReason) {
        logger.error(logTag, message)
        appCoroutineScope.launch {
            terminateSession(
                finalState = HolderSessionState.Complete.Failed(
                    SessionError(message = message, reason = reason)
                ),
                sendEndCommand = true
            )
        }
    }

    private fun safeTransitionTo(
        state: HolderSessionState,
        logMessage: String = CANNOT_TRANSITION_TO_STATE.format(
            sessionFlow.value.currentState.value,
            state
        ),
        exceptionWrapper: ((String, Throwable) -> Exception)? = null
    ) {
        if (sessionFlow.value.currentState.value == state) {
            return
        }

        try {
            sessionFlow.value.transitionTo(state)
            if (state.isComplete()) {
                sessionTimer.stop()
            }
            logger.debug(logTag, "$TRANSITION_SUCCESSFUL_TO_STATE $state")
        } catch (exception: IllegalStateException) {
            val loggedException = exceptionWrapper?.invoke(logMessage, exception) ?: exception
            logger.error(logTag, logMessage, loggedException)
        }
    }

    private fun handlePeerTermination(status: SessionDataStatus) {
        val currentState = holderSessionState.value
        logger.debug(logTag, "Peer termination received (status: $status) in state: $currentState")

        when (currentState) {
            is HolderSessionState.AwaitingVerifierResolution -> {
                if (status == SessionDataStatus.SESSION_TERMINATION) {
                    safeTransitionTo(HolderSessionState.Complete.Success())
                } else {
                    safeTransitionTo(
                        HolderSessionState.Complete.Failed(
                            SessionError(
                                message = "Status error: unexpected status ${status.code}",
                                reason = SessionErrorReason.StatusError(status.code)
                            )
                        )
                    )
                }
                logger.debug(logTag, STOPPING_BLE_ADVERTISING)
                stopAdvertising(sendEndCommand = false)
                logger.debug(logTag, "Holder session terminated")
            }

            else -> {
                logger.debug(logTag, STOPPING_BLE_ADVERTISING)
                stopAdvertising(sendEndCommand = false)
                safeTransitionTo(
                    HolderSessionState.Complete.Failed(
                        SessionError(
                            message = "Peer terminated session (status: $status)",
                            reason = SessionErrorReason.PeerTermination
                        )
                    )
                )
            }
        }
    }

    private fun deviceRequestContainsPortrait(deviceRequest: DeviceRequest): Boolean =
        deviceRequest.docRequests.any { docRequest ->
            docRequest.itemsRequest.nameSpaces.any { (namespace, elements) ->
                namespace == DocumentType.Mdl.NAMESPACE &&
                    elements.containsKey(MdlAttribute.Portrait.value)
            }
        }

    private fun handleConnectionLoss(address: String? = null, isGattEnd: Boolean = false) {
        val currentState = holderSessionState.value

        if (currentState.isComplete() || currentState is HolderSessionState.SendingTermination) {
            return
        }

        when (currentState) {
            is HolderSessionState.ReadyToPresent,
            is HolderSessionState.PresentingEngagement -> {
                val finalState = if (isGattEnd) {
                    HolderSessionState.Complete.Cancelled
                } else {
                    val message = address?.let { "Device $it disconnected unexpectedly" }
                        ?: "Connection lost unexpectedly"
                    HolderSessionState.Complete.Failed(
                        SessionError(
                            message = message,
                            reason = SessionErrorReason.InvalidBluetoothState(
                                BluetoothDisconnectedException(
                                    "Bluetooth disconnected unexpectedly",
                                    IllegalStateException(message)
                                )
                            )
                        )
                    )
                }
                appCoroutineScope.launch { terminateSession(finalState) }
            }

            is HolderSessionState.ProcessingEstablishment,
            is HolderSessionState.AwaitingUserConsent,
            is HolderSessionState.ProcessingResponse -> {
                val message = address?.let { "Device $it disconnected unexpectedly" }
                    ?: "Connection lost unexpectedly"
                failWith(
                    message = message,
                    reason = SessionErrorReason.InvalidBluetoothState(
                        BluetoothDisconnectedException(
                            "Bluetooth disconnected unexpectedly",
                            IllegalStateException(message)
                        )
                    )
                )
            }

            is HolderSessionState.AwaitingVerifierResolution -> {
                appCoroutineScope.launch {
                    terminateSession(finalState = HolderSessionState.Complete.Success())
                }
            }

            else -> {
                appCoroutineScope.launch {
                    terminateSession(finalState = HolderSessionState.Complete.Cancelled)
                }
            }
        }
    }

    private suspend fun terminateSession(
        finalState: HolderSessionState,
        sessionDataToSend: ByteArray? = null,
        sendEndCommand: Boolean = false
    ) {
        val context = currentContext
        safeTransitionTo(HolderSessionState.SendingTermination)

        var sent = sessionDataToSend == null
        if (sessionDataToSend != null) {
            sent = peripheralBluetoothTransport.sendMessage(
                serviceUuid = context.sessionUuid,
                data = sessionDataToSend
            )
            sessionTimer.reset()
        }

        if (sent && sessionDataToSend != null) {
            holderSessionTerminator.terminate(context.sessionUuid)
        } else {
            stopAdvertising(sendEndCommand = sendEndCommand)
        }

        safeTransitionTo(finalState)
    }

    private companion object {
        val INACTIVITY_TIMEOUT = 300.seconds
        const val UNKNOWN_ERROR = "Unknown error"
        const val PORTRAIT_POLICY_VIOLATION =
            "Policy violation: DeviceRequest does not request portrait attribute"
        const val UNRECOGNISED_MESSAGE =
            "Sequencing violation: inbound message is not a recognised type"
        const val STOPPING_BLE_ADVERTISING = "Stopping BLE advertising"
    }
}
