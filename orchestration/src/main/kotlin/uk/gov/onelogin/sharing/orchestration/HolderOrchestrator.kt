package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPrivateKey
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
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransportError
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
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
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.security.cryptography.usecases.DecryptDeviceRequestUseCase

@Suppress("LongParameterList")
@SingleIn(AppScope::class)
@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Holder>())
class HolderOrchestrator(
    private val logger: Logger,
    private val sessionFactory: SessionFactory<HolderSession>,
    private val peripheralBluetoothTransport: PeripheralBluetoothTransport,
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope,
    private val decryptDeviceRequestUseCase: DecryptDeviceRequestUseCase,
    private val prerequisiteGate: PrerequisiteGate,
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProvider
) : Orchestrator.Holder {
    private val sessionFlow = MutableStateFlow(sessionFactory.create())

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

        if (holderSessionState.value !is HolderSessionState.NotStarted) {
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
            prerequisiteGate.checkPrerequisites(
                Prerequisite.BLUETOOTH
            )[Prerequisite.BLUETOOTH].also {
                logger.debug(
                    logTag,
                    completedPrerequisiteChecks(
                        journey = Orchestrator.Holder.JOURNEY_NAME,
                        response = it
                    )
                )
            }?.let { prerequisiteCheck ->
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

    private fun handleStartPrerequisiteCheck(prerequisiteCheck: PrerequisiteResponse) {
        when (prerequisiteCheck) {
            PrerequisiteResponse.MeetsPrerequisites -> {
                safeTransitionTo(HolderSessionState.ReadyToPresent)

                appCoroutineScope.launch {
                    peripheralBluetoothTransport.state.collect {
                        handleMdocState(it)
                    }
                }

                appCoroutineScope.launch {
                    peripheralBluetoothTransport.start(
                        serviceUuid = sessionFlow.value.sessionContext.sessionUuid
                    )
                }

                val qrCode = sessionFlow.value.sessionContext.qrCode
                if (qrCode.isNotEmpty()) {
                    safeTransitionTo(HolderSessionState.PresentingEngagement(qrCode))
                }
            }

            is PrerequisiteResponse.Incapable,
            is PrerequisiteResponse.NotReady,
            is PrerequisiteResponse.Unauthorized ->
                safeTransitionTo(
                    HolderSessionState.Preflight(
                        mapOf(
                            Prerequisite.BLUETOOTH to prerequisiteCheck
                        )
                    )
                )
        }
    }

    override fun cancel() {
        safeTransitionTo(
            state = HolderSessionState.Complete.Cancelled,
            exceptionWrapper = ::OrchestratorCannotCancelException
        )

        stopAdvertising()
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

    private fun stopAdvertising() {
        appCoroutineScope.launch {
            peripheralBluetoothTransport.stop()
        }
    }

    @Suppress("ComplexMethod", "LongMethod")
    private fun handleMdocState(state: PeripheralBluetoothState) {
        logger.debug(logTag, "state = $state")

        when (state) {
            PeripheralBluetoothState.AdvertisingStarted -> {
                logger.debug(
                    logTag,
                    "Mdoc - Advertising Started UUID: " +
                        "${sessionFlow.value.sessionContext.sessionUuid}"
                )
            }

            PeripheralBluetoothState.AdvertisingStopped -> {
                logger.debug(logTag, "Mdoc - Advertising Stopped")
            }

            is PeripheralBluetoothState.Connected -> {
                safeTransitionTo(HolderSessionState.ProcessingEstablishment)

                logger.debug(logTag, "Mdoc - Connected: ${state.address}")
            }

            is PeripheralBluetoothState.Disconnected -> {
                @RequiresImplementation(
                    details = [
                        ImplementationDetail(
                            ticket = "DCMAW-16898",
                            description = "We may need to handle explicit bluetooth" +
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
                } else {
                    logger.debug(logTag, "Error Mdoc - Disconnected: ${state.address}")
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

                stopAdvertising()
            }

            is PeripheralBluetoothState.Error -> {
                handleError(state.reason)
            }

            PeripheralBluetoothState.GattServiceStopped -> {
                logger.debug(logTag, "Mdoc - GattService Stopped")
            }

            PeripheralBluetoothState.Idle -> {
                logger.debug(logTag, "Mdoc - Idle")
            }

            is PeripheralBluetoothState.ServiceAdded ->
                logger.debug(logTag, "Mdoc - Service Added: ${state.uuid}")

            is PeripheralBluetoothState.PeripheralBluetoothEnded -> {
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
                val keypair = sessionFlow.value.sessionContext.keyPair?.private
                if (keypair !is ECPrivateKey) {
                    logger.error(
                        logTag,
                        "Invalid or missing keypair"
                    )
                    return
                }

                val deviceRequest = decryptDeviceRequestUseCase.execute(
                    sessionEstablishmentBytes = state.message,
                    engagement = sessionFlow.value.sessionContext.engagement,
                    holderPrivateKey = keypair,
                    decryptCounter = sessionFlow.value.sessionContext.decryptCounter
                )

                // only increment decrypt counter if decryption was successful
                sessionFlow.value.updateSessionContext {
                    it.copy(decryptCounter = it.decryptCounter + 1u)
                }

                safeTransitionTo(HolderSessionState.AwaitingUserConsent(deviceRequest))
            }
        }
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
