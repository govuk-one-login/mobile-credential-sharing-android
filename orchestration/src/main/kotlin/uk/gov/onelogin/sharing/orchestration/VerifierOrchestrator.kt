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
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.scanner.QrParser
import uk.gov.onelogin.sharing.cryptoService.scanner.QrScanResult
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
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
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
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
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope,
    private val barcodeParser: QrParser,
    private val centralBluetoothTransport: CentralBluetoothTransport,
    private val verifierCryptoService: VerifierCryptoService
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

        logger.debug(logTag, "AttributeGroup: ${verifierConfig.verificationRequest.attributeGroup}")
        performPreflightChecks()
    }

    private fun performPreflightChecks() {
        try {
            val prerequisites = listOf(
                Prerequisite.BLUETOOTH,
                Prerequisite.CAMERA
            )

            val prerequisiteResponse = prerequisiteGate.evaluatePrerequisites(prerequisites).also {
                logger.debug(
                    logTag,
                    completedPrerequisiteChecks(
                        Orchestrator.Verifier.JOURNEY_NAME,
                        it
                    )
                )
            }

            if (prerequisiteResponse.isEmpty()) {
                safeTransitionTo(VerifierSessionState.ReadyToScan)
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

    private fun handleStartPrerequisiteFailure(missingPrerequisites: List<MissingPrerequisite>) {
        if (missingPrerequisites.any { !it.isRecoverable }) {
            VerifierSessionState.Complete.Failed(
                SessionError(
                    "Device cannot perform journey",
                    SessionErrorReason.UnrecoverablePrerequisite(
                        missingPrerequisites.filter { !it.isRecoverable }
                    )
                )
            )
        } else {
            VerifierSessionState.Preflight(
                missingPrerequisites = missingPrerequisites,
                onComplete = ::performPreflightChecks
            )
        }
            .let { safeTransitionTo(state = it, logMessage = START_ORCHESTRATION_ERROR) }
    }

    override fun processQrCode(qrCode: String?) {
        val result = barcodeParser.parse(qrCode)

        if (result is QrScanResult.NotFound) return

        safeTransitionTo(VerifierSessionState.ProcessingEngagement)

        when (result) {
            is QrScanResult.Success -> {
                runCatching {
                    verifierCryptoService.processEngagement(result.value) { context ->
                        sessionFlow.value.updateCryptoContext { context }
                        context
                    }
                }.onFailure { e ->
                    failWith("Error processing engagement: ${e.message}", e as Exception)
                }.onSuccess {
                    sessionFlow.value.cryptoContext.serviceUuid?.let { uuid ->
                        safeTransitionTo(VerifierSessionState.Connecting)
                        centralBluetoothTransport.scanAndConnect(uuid)
                    } ?: failWith(
                        "Service UUID not found in device engagement",
                        IllegalStateException("Service UUID not found in device engagement")
                    )
                }
            }

            is QrScanResult.Invalid -> {
                safeTransitionTo(
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = result.rawValue,
                            exception = IllegalArgumentException("Qr Code is an unsupported format")
                        )
                    )
                )
            }

            QrScanResult.NotFound -> Unit
        }
    }

    override fun cancel() {
        if (sessionFlow.value.isComplete()) return

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
        if (sessionFlow.value.isComplete()) return

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

            is CentralBluetoothState.Error -> {
                stopCentralTransport()
                failWith(
                    "Bluetooth error: ${state.reason}",
                    IllegalStateException("Bluetooth error: ${state.reason}")
                )
            }

            is CentralBluetoothState.CentralBluetoothEnded -> {
                stopCentralTransport()
            }

            else -> Unit
        }
    }

    private fun failWith(message: String, exception: Exception) {
        logger.error(logTag, message, exception)
        safeTransitionTo(
            VerifierSessionState.Complete.Failed(
                SessionError(message = message, exception = exception)
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
