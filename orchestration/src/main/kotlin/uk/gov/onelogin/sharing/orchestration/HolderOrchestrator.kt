package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPrivateKey
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
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransportError
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cryptography.usecases.DecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
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
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory

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
    private val prerequisiteGate: PrerequisiteGate.V2,
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProvider
) : Orchestrator.Holder {
    private var transportStateJob: Job? = null
    private val sessionFlow = MutableStateFlow(sessionFactory.create())

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

    private fun handleStartPrerequisiteCheck(prerequisiteCheck: List<MissingPrerequisiteV2>) {
        if (prerequisiteCheck.isEmpty()) {
            safeTransitionTo(HolderSessionState.ReadyToPresent)

            appCoroutineScope.launch {
                peripheralBluetoothTransport.start(
                    serviceUuid = sessionFlow.value.sessionContext.sessionUuid
                )
            }

            val qrCode = sessionFlow.value.sessionContext.qrCode
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
                serviceUuid = sessionFlow.value.sessionContext.sessionUuid,
                sendEndCommand = sendEndCommand
            )
        }
    }

    @Suppress("LongMethod")
    private fun handleMdocState(state: PeripheralBluetoothState) {
        logger.debug(logTag, "state = $state")

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

                    stopAdvertising(sendEndCommand = true)

                    safeTransitionTo(
                        HolderSessionState.Complete.Failed(
                            SessionError(
                                "Device ${state.address} disconnected unexpectedly",
                                BluetoothDisconnectedException(
                                    "Bluetooth disconnected unexpectedly",
                                    IllegalStateException(
                                        "Device ${state.address} disconnected unexpectedly"
                                    )
                                )
                            )
                        )
                    )
                }
            }

            is PeripheralBluetoothState.Error -> {
                handleError(state.reason)
            }

            PeripheralBluetoothState.Idle -> Unit

            is PeripheralBluetoothState.Ended -> {
                safeTransitionTo(HolderSessionState.Complete.Cancelled)

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
        val keypair = sessionFlow.value.sessionContext.keyPair?.private
        if (keypair !is ECPrivateKey) {
            sendTerminationAndFail(IllegalStateException("Invalid or missing keypair"))
            return
        }

        try {
            val deviceRequest = decryptDeviceRequestUseCase.execute(
                sessionEstablishmentBytes = message,
                engagement = sessionFlow.value.sessionContext.engagement,
                holderPrivateKey = keypair,
                decryptCounter = sessionFlow.value.sessionContext.decryptCounter,
                onDeriveSkDevice = { skDevice ->
                    sessionFlow.value.updateSessionContext {
                        it.copy(skDevice = skDevice)
                    }
                }
            )

            sessionFlow.value.updateSessionContext {
                it.copy(decryptCounter = it.decryptCounter + 1u)
            }

            safeTransitionTo(HolderSessionState.AwaitingUserConsent(deviceRequest))
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            sendTerminationAndFail(e)
        }
    }

    fun assembleAndEncryptResponse(documents: List<Document>): ByteArray {
        val deviceResponse = DeviceResponse(
            documents = documents,
            documentErrors = null
        )
        val context = sessionFlow.value.sessionContext
        val skDevice = context.skDevice
            ?: error("Missing skDevice")

        val encryptedResponse = holderCryptoService.encryptDeviceResponse(
            deviceResponse = deviceResponse,
            skDevice = skDevice,
            encryptCounter = context.encryptCounter
        )

        sessionFlow.value.updateSessionContext {
            it.copy(encryptCounter = it.encryptCounter + 1u)
        }

        return encryptedResponse
    }

    private fun sendTerminationAndFail(exception: Exception) {
        logger.error(logTag, exception.message ?: "Unknown error", exception)
        holderCryptoService.buildTerminationSessionData(SessionDataStatus.SESSION_TERMINATION)
        safeTransitionTo(
            HolderSessionState.Complete.Failed(
                SessionError(
                    message = exception.message ?: "Unknown error",
                    exception = exception
                )
            )
        )
    }

    private fun handleError(reason: PeripheralBluetoothTransportError) {
        when (reason) {
            PeripheralBluetoothTransportError.ADVERTISING_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Advertising failed")

            PeripheralBluetoothTransportError.GATT_NOT_AVAILABLE ->
                logger.debug(logTag, "Mdoc - Error: GATT not available")

            PeripheralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING ->
                logger.debug(logTag, "Mdoc - Error: Bluetooth permission missing")

            PeripheralBluetoothTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Descriptor write request failed")
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
        try {
            sessionFlow.value.transitionTo(state)
            logger.debug(logTag, "$TRANSITION_SUCCESSFUL_TO_STATE $state")
        } catch (exception: IllegalStateException) {
            val loggedException = exceptionWrapper?.invoke(logMessage, exception) ?: exception
            logger.error(logTag, logMessage, loggedException)
        }
    }
}
