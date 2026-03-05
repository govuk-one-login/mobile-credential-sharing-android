package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPrivateKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.Logger
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.completedPrerequisiteChecks
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.createSessionResetMessage
import uk.gov.onelogin.orchestration.Orchestrator.LogMessages.recreateSessionOnStartMessage
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransportError
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.security.cryptography.usecases.DecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.security.engagement.GenerateEngagementQrCode

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Holder>())
class HolderOrchestrator(
    private val logger: Logger,
    private val sessionFactory: SessionFactory<HolderSession>,
    private val mdocPeripheralTransport: MdocPeripheralTransport,
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope,
    private val decryptDeviceRequestUseCase: DecryptDeviceRequestUseCase,
    private val prerequisiteGate: PrerequisiteGate,
) : Orchestrator.Holder {
    private var session: HolderSession = sessionFactory.create()
    override val holderSessionState: SharedFlow<HolderSessionState> = session.currentState

    override fun start() {
        if (session.isComplete()) {
            session = sessionFactory.create().also {
                logger.debug(
                    logTag,
                    recreateSessionOnStartMessage(Orchestrator.Holder.JOURNEY_NAME)
                )
            }
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

                appCoroutineScope.launch {
                    mdocPeripheralTransport.state.collect {
                        handleMdocState(it)
                    }
                }

                appCoroutineScope.launch {
                    mdocPeripheralTransport.start(
                        serviceUuid = session.sessionContext.sessionUuid,
                    )
                }

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
                session.transitionTo(HolderSessionState.ReadyToPresent)
                val qrCode = session.sessionContext.qrCode
                if (qrCode.isNotEmpty()) {
                    session.transitionTo(
                        HolderSessionState.PresentingEngagement(qrCode)
                    )
                }
            }

            is PrerequisiteResponse.Incapable,
            is PrerequisiteResponse.NotReady,
            is PrerequisiteResponse.Unauthorized ->
                session.transitionTo(
                    HolderSessionState.Preflight(
                        mapOf(
                            Prerequisite.BLUETOOTH to prerequisiteCheck
                        )
                    )
                )
        }
    }

    override fun cancel() {
        try {
            session.transitionTo(
                HolderSessionState.Complete.Cancelled
            )
            logger.debug(logTag, CANCEL_ORCHESTRATION_SUCCESS)
        } catch (exception: IllegalStateException) {
            CANCEL_ORCHESTRATION_ERROR.let { logMessage ->
                logger.error(
                    logTag,
                    logMessage,
                    OrchestratorCannotCancelException(logMessage, exception)
                )
            }
        }

        stopAdvertising()
    }

    override fun reset() {
        session = sessionFactory.create().also {
            logger.debug(
                logTag,
                createSessionResetMessage(Orchestrator.Holder.JOURNEY_NAME)
            )
        }
    }

    private fun stopAdvertising() {
        appCoroutineScope.launch {
            mdocPeripheralTransport.stop()
        }
    }

    private fun handleMdocState(state: MdocPeripheralState) {
        logger.debug(logTag, "state = $state")

        when (state) {
            MdocPeripheralState.AdvertisingStarted -> {
                logger.debug(
                    logTag,
                    "Mdoc - Advertising Started UUID: " +
                            "${session.sessionContext.sessionUuid}"
                )
            }

            MdocPeripheralState.AdvertisingStopped -> {
                logger.debug(logTag, "Mdoc - Advertising Stopped")
            }

            is MdocPeripheralState.Connected ->
                logger.debug(logTag, "Mdoc - Connected: ${state.address}")

            is MdocPeripheralState.Disconnected -> {
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
//                    TODO("pass error to view model")
                    /*_uiState.update {
                        it.copy(
                            connectedAddress = state.address,
                            showErrorScreen = true,
                            bluetoothErrorType = BluetoothUiErrorTypes.BLUETOOTH_DISCONNECTED
                        )
                    }*/
                }

                stopAdvertising()

            }

            is MdocPeripheralState.Error -> {
                handleError(state.reason)
            }

            MdocPeripheralState.GattServiceStopped -> {
                logger.debug(logTag, "Mdoc - GattService Stopped")
            }

            MdocPeripheralState.Idle -> {
                logger.debug(logTag, "Mdoc - Idle")
            }

            is MdocPeripheralState.ServiceAdded ->
                logger.debug(logTag, "Mdoc - Service Added: ${state.uuid}")

            is MdocPeripheralState.MdocPeripheralEnded -> {
                if (state.status == SessionEndStates.SUCCESS) {
                    logger.debug(logTag, "Mdoc - Ending session")
                } else {
//                    T
//                    _uiState.update { it.copy(showErrorScreen = true) }
                    logger.error(
                        logTag,
                        "Mdoc - Error while ending session: ${state.status}"
                    )
                }
            }

            is MdocPeripheralState.MessageReceived -> {
                val deviceRequest = decryptDeviceRequestUseCase.execute(
                    sessionEstablishmentBytes = state.message,
                    engagement = session.sessionContext.engagement,
                    holderPrivateKey = session.sessionContext.keyPair?.private as ECPrivateKey
                )

                try {
                    session.transitionTo(
                        HolderSessionState.RequestReceived(deviceRequest)
                    )
                } catch (exception: IllegalStateException) {
                    logger.error(
                        logTag,
                        "Cannot transition to request received state",
                        exception
                    )
                }

                deviceRequest
                    .docRequests.firstOrNull()
                    ?.itemsRequest
                    ?.nameSpaces
                    ?.forEach { (key, value) ->
                        logger.debug(logTag, "Requests: key = $key, value = $value")
                    }
            }
        }
    }

    private fun handleError(reason: MdocPeripheralTransportError) {
        when (reason) {
            MdocPeripheralTransportError.ADVERTISING_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Advertising failed")

            MdocPeripheralTransportError.GATT_NOT_AVAILABLE ->
                logger.debug(logTag, "Mdoc - Error: GATT not available")

            MdocPeripheralTransportError.BLUETOOTH_PERMISSION_MISSING ->
                logger.debug(logTag, "Mdoc - Error: Bluetooth permission missing")

            MdocPeripheralTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED ->
                logger.debug(logTag, "Mdoc - Error: Descriptor write request failed")
        }
    }

}
