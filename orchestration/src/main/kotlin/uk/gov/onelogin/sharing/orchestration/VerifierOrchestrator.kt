package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransportError
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.decodeDeviceEngagement
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
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate.Companion.meetsPrerequisites
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSession
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@Suppress("LongParameterList")
@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
@SingleIn(AppScope::class)
class VerifierOrchestrator(
    private val logger: Logger,
    private val prerequisiteGate: PrerequisiteGate,
    private val sessionFactory: SessionFactory<VerifierSession>,
    @Suppress("UnusedPrivateProperty")
    private val verifierConfig: VerifierConfig,
    private val centralBluetoothTransport: CentralBluetoothTransport,
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope
) : Orchestrator.Verifier {

    private val sessionFlow = MutableStateFlow(sessionFactory.create())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val verifierSessionState: StateFlow<VerifierSessionState> = sessionFlow.flatMapLatest {
        it.currentState
    }.stateIn(
        appCoroutineScope,
        SharingStarted.Eagerly,
        sessionFlow.value.currentState.value
    )

    init {
        appCoroutineScope.launch {
            centralBluetoothTransport.state.collect { handleCentralBluetoothState(it) }
        }
    }

    override fun start() {
        if (sessionFlow.value.isComplete()) {
            sessionFlow.update {
                sessionFactory.create().also {
                    logger.debug(
                        logTag,
                        recreateSessionOnStartMessage(Orchestrator.Verifier.JOURNEY_NAME)
                    )
                }
            }
        }

        if (sessionFlow.value.currentState.value !is VerifierSessionState.NotStarted) {
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

        try {
            val prerequisites = listOf(
                Prerequisite.BLUETOOTH,
                Prerequisite.CAMERA
            )

            val prerequisiteResponse = prerequisiteGate.checkPrerequisites(prerequisites).also {
                logger.debug(
                    logTag,
                    completedPrerequisiteChecks(
                        Orchestrator.Verifier.JOURNEY_NAME,
                        it
                    )
                )
            }

            if (prerequisiteResponse.meetsPrerequisites()) {
                safeTransitionTo(
                    VerifierSessionState.ReadyToScan
                )
            } else {
                handleStartPrerequisiteFailure(prerequisiteResponse)
            }
            logger.debug(logTag, START_ORCHESTRATION_SUCCESS)
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

    private fun handleStartPrerequisiteFailure(
        responseMap: Map<Prerequisite, PrerequisiteResponse>
    ) {
        responseMap.filterValues {
            PrerequisiteResponse.MeetsPrerequisites != it
        }
            .let(VerifierSessionState::Preflight)
            .let { safeTransitionTo(state = it, logMessage = START_ORCHESTRATION_ERROR) }
    }

    override fun processQrCode(qrCode: BarcodeDataResult) {
        when (qrCode) {
            is BarcodeDataResult.Valid -> {
                safeTransitionTo(
                    VerifierSessionState.ProcessingEngagement
                )

                val engagementData = decodeDeviceEngagement(qrCode.data, logger)
                val serviceUuid = engagementData?.getFirstPeripheralServerModeUuid()

                if (serviceUuid != null) {
                    safeTransitionTo(VerifierSessionState.Connecting)
                    centralBluetoothTransport.scanAndConnect(serviceUuid)
                } else {
                    logger.error(logTag, "No service UUID found in engagement data")
                    safeTransitionTo(
                        VerifierSessionState.Complete.Failed(
                            SessionError(
                                message = "No service UUID in engagement data",
                                exception = IllegalArgumentException(
                                    "No service UUID in engagement data"
                                )
                            )
                        )
                    )
                }
            }

            is BarcodeDataResult.Invalid -> {
                safeTransitionTo(
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = qrCode.data,
                            exception = IllegalArgumentException(
                                "Qr Code is an unsupported format"
                            )
                        )
                    )
                )
            }

            else -> Unit
        }
    }

    override fun cancel() {
        safeTransitionTo(
            state = VerifierSessionState.Complete.Cancelled,
            exceptionWrapper = ::OrchestratorCannotCancelException
        )

        stopCentralTransport()
    }

    override fun reset() {
        sessionFlow.update {
            sessionFactory.create().also {
                logger.debug(
                    logTag,
                    createSessionResetMessage(Orchestrator.Verifier.JOURNEY_NAME)
                )
            }
        }
    }

    private fun stopCentralTransport() {
        appCoroutineScope.launch { centralBluetoothTransport.stop() }
    }

    private fun handleCentralBluetoothState(state: CentralBluetoothState) {
        logger.debug(logTag, "BLE state = $state")

        when (state) {
            is CentralBluetoothState.Disconnected -> {
                if (state.isSessionEnd) return

                stopCentralTransport()

                safeTransitionTo(
                    VerifierSessionState.Complete.Failed(
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

            is CentralBluetoothState.Error ->
                handleError(state.reason)

            is CentralBluetoothState.CentralBluetoothEnded -> {
                stopCentralTransport()
            }

            else -> Unit
        }
    }

    private fun handleError(reason: CentralBluetoothTransportError) {
        logger.error(logTag, "BLE - Error: $reason")

        stopCentralTransport()

        safeTransitionTo(
            VerifierSessionState.Complete.Failed(
                SessionError(
                    message = "Bluetooth error: $reason",
                    exception = IllegalStateException("Bluetooth error: $reason")
                )
            )
        )
    }

    private fun safeTransitionTo(
        state: VerifierSessionState,
        logMessage: String = "$CANNOT_TRANSITION_TO_STATE $state",
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
