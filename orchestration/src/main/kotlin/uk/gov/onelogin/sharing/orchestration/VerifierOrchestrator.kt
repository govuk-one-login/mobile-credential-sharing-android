package uk.gov.onelogin.sharing.orchestration

import androidx.annotation.Keep
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
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.scanner.QrParser
import uk.gov.onelogin.sharing.cryptoService.scanner.QrScanResult
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.cryptoService.verifier.SessionEstablishmentException
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoContext
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
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
import uk.gov.onelogin.sharing.orchestration.verificationrequest.toItemsRequest
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSession
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.verification.document.DocumentVerifier
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status as DeviceResponseStatus

@Keep
@Suppress("LongParameterList", "TooManyFunctions")
@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
@SingleIn(AppScope::class)
class VerifierOrchestrator(
    private val logger: Logger,
    private val prerequisiteGate: PrerequisiteGate,
    private val sessionFactory: SessionFactory<VerifierSession>,
    private val verifierConfig: VerifierConfig,
    @param:ApplicationScope private val appCoroutineScope: CoroutineScope,
    private val barcodeParser: QrParser,
    private val centralBluetoothTransport: CentralBluetoothTransport,
    private val verifierCryptoService: VerifierCryptoService,
    private val documentVerifier: DocumentVerifier,
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
            centralBluetoothTransport.state.collect(::handleCentralBluetoothState)
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
        if (missingPrerequisites.any { !it.isRecoverable() }) {
            VerifierSessionState.Complete.Failed(
                SessionError(
                    "Device cannot perform journey",
                    SessionErrorReason.UnrecoverablePrerequisite(
                        missingPrerequisites.filter { !it.isRecoverable() }
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

    override suspend fun processQrCode(qrCode: String?) {
        val result = barcodeParser.parse(qrCode)

        if (result is QrScanResult.NotFound) return

        safeTransitionTo(VerifierSessionState.ProcessingEngagement)

        when (result) {
            is QrScanResult.Success -> {
                runCatching {
                    verifierCryptoService.establishSession(result.value) { context ->
                        sessionFlow.value.updateCryptoContext { context }
                        context
                    }
                }.onFailure { e ->
                    failWith(
                        "Error processing engagement: ${e.message}",
                        SessionErrorReason.CannotProcessEngagement(result.value),
                        e
                    )
                }.onSuccess {
                    sessionFlow.value.cryptoContext?.let {
                        safeTransitionTo(VerifierSessionState.Connecting)
                        centralBluetoothTransport.start(it.serviceUuid)
                    } ?: failWith(
                        "Service UUID not found in device engagement",
                        SessionErrorReason.ServiceUuidNotFound
                    )
                }
            }

            is QrScanResult.Invalid -> {
                result.rawValue
                    .let(SessionErrorReason::UnsupportedQrCodeFormat)
                    .let {
                        failWith(
                            "Qr code is an unsupported format: ${it.rawValue}",
                            it
                        )
                    }
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

    private suspend fun handleCentralBluetoothState(state: CentralBluetoothState) {
        if (sessionFlow.value.isComplete()) return

        logger.debug(logTag, "BLE state = $state")

        when (state) {
            is CentralBluetoothState.ConnectionStateStarted -> handleConnectionStateStarted()

            is CentralBluetoothState.Disconnected -> {
                if (state.isSessionEnd) return

                failWith(
                    "Device ${state.address} disconnected unexpectedly",
                    SessionErrorReason.InvalidBluetoothState(
                        BluetoothDisconnectedException(
                            "Bluetooth disconnected unexpectedly",
                            IllegalStateException(
                                "Device ${state.address} disconnected unexpectedly"
                            )
                        )
                    )
                )
            }

            is CentralBluetoothState.Error -> {
                failWith(
                    "Bluetooth error: ${state.reason}",
                    SessionErrorReason.InvalidBluetoothState(
                        IllegalStateException("Bluetooth error: ${state.reason}")
                    )
                )
            }

            is CentralBluetoothState.CentralBluetoothEnded -> {
                stopCentralTransport()
            }

            is CentralBluetoothState.Message -> handleCentralBluetoothStateMessage(state)

            is CentralBluetoothState.Connected,
            CentralBluetoothState.Connecting,
            is CentralBluetoothState.Idle,
            is CentralBluetoothState.Scanning,
                -> Unit
        }
    }

    private fun handleCentralBluetoothStateMessage(state: CentralBluetoothState.Message) {
        val sessionData = runCatching {
            verifierCryptoService.deserializeSessionData(state.value).apply {
                check(hasOkStatus()) {
                    "Received SessionData error status: ${status?.code}"
                }
                check(hasData()) {
                    "Received empty SessionData payload"
                }
            }
        }.onFailure { throwable ->
            failWith(
                "Received invalid SessionData instance",
                SessionErrorReason.InvalidSessionDataPayload,
                throwable
            )
        }.getOrNull() ?: return

        logger.debug(logTag, "Deserialized SessionData from bluetooth central Message")

        safeTransitionTo(VerifierSessionState.Verifying)

        val context = sessionFlow.value.cryptoContext ?: return failWith(
            "Missing crypto context when decrypting DeviceResponse",
            SessionErrorReason.MissingCryptoContext
        )

        runCatching {
            verifierCryptoService.decryptDeviceResponse(
                deviceResponseBytes = sessionData.data!!,
                skDevice = context.skDevice,
                decryptCounter = context.decryptCounter
            )
        }.onSuccess { deviceResponse ->
            sessionFlow.value.updateCryptoContext {
                context.copy(decryptCounter = context.decryptCounter + 1u)
            }.also {
                val updatedContext = sessionFlow.value.cryptoContext ?: return@onSuccess
                logger.debug(
                    logTag,
                    "Decrypt counter incremented to: ${updatedContext.decryptCounter}"
                )
            }

            evaluateDeviceResponse(deviceResponse)
        }.onFailure { _ ->
            stopCentralTransport()
            safeTransitionTo(
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        message = "Error decrypting DeviceResponse",
                        reason = SessionErrorReason.CannotDecryptDeviceResponse
                    )
                )
            )
        }
    }

    private fun evaluateDeviceResponse(deviceResponse: DeviceResponse) {
        val status = deviceResponse.status

        if (status != DeviceResponseStatus.OK) {
            failWith(
                "DeviceRequest processing error: status ${status.code}",
                SessionErrorReason.DeviceRequestProcessingError(status.code)
            )
            return
        }

        val documents = deviceResponse.documents
        if (documents.isNullOrEmpty()) {
            failWith(
                "Document not returned: status ${status.code}",
                SessionErrorReason.DocumentNotReturned
            )
            return
        }

        verifyDocuments(deviceResponse)
    }

    private fun verifyDocuments(deviceResponse: DeviceResponse) {
        try {
            deviceResponse.documents!!.forEach { document ->
                documentVerifier.verifyDocument(
                    document,
                    sessionFlow.value.cryptoContext?.sessionTranscriptBytes
                )
            }
            safeTransitionTo(
                VerifierSessionState.Complete.Success(deviceResponse)
            )
        } catch (exception: VerificationResult.Failure) {
            failWith(
                "Failed to verify provided documents (${exception.error})",
                SessionErrorReason.UnverifiableDocument(exception.error),
                exception
            )
        } finally {
            stopCentralTransport()
        }
    }

    private suspend fun handleConnectionStateStarted() {
        val context = sessionFlow.value.cryptoContext ?: return failWith(
            "Missing crypto context when building DeviceRequest",
            SessionErrorReason.MissingCryptoContext
        )

        val itemsRequest = verifierConfig.verificationRequest.attributeGroup
            .toItemsRequest(verifierConfig.verificationRequest.documentType)

        runCatching {
            buildAndSendSessionEstablishment(context, itemsRequest)
        }.onFailure { e ->
            val reason = when (e) {
                is EncryptDeviceRequestException -> SessionErrorReason.CannotEncryptDeviceRequest

                is SessionEstablishmentException ->
                    SessionErrorReason.CannotBuildSessionEstablishment

                else -> SessionErrorReason.CannotSendMessage
            }
            failWith(e.message ?: "Error building SessionEstablishment", reason)
        }
    }

    private suspend fun buildAndSendSessionEstablishment(
        context: VerifierCryptoContext,
        itemsRequest: ItemsRequest,
    ) {
        val deviceRequestBytes = verifierCryptoService.buildDeviceRequest(itemsRequest)
        val encryptedDeviceRequest = verifierCryptoService.encryptDeviceRequest(
            deviceRequestBytes = deviceRequestBytes,
            skReader = context.skReader,
            encryptCounter = context.encryptCounter
        )
        sessionFlow.value.updateCryptoContext {
            context.copy(encryptCounter = context.encryptCounter + 1u)
        }
        logger.debug(logTag, "Encrypted DeviceRequest: ${encryptedDeviceRequest.toHexString()}")
        verifierCryptoService.buildSessionEstablishment(
            eReaderKeyBytes = context.eReaderKeyTagged,
            encryptedDeviceRequest = encryptedDeviceRequest
        ).also { sessionEstablishmentBytes ->
            logger.debug(
                logTag,
                "SessionEstablishment bytes: ${sessionEstablishmentBytes.toHexString()}"
            )
            val sent = centralBluetoothTransport.sendMessage(
                serviceUuid = context.serviceUuid,
                data = sessionEstablishmentBytes
            )
            if (!sent) error("Failed to send SessionEstablishment message")
        }
    }

    private fun failWith(message: String, exception: Throwable) = failWith(
        message = message,
        reason = SessionErrorReason.UnrecoverableThrowable(exception),
        throwable = exception
    )

    private fun failWith(message: String, reason: SessionErrorReason) {
        logger.error(logTag, message)
        stopCentralTransport()
        safeTransitionTo(
            VerifierSessionState.Complete.Failed(
                SessionError(message = message, reason = reason)
            )
        )
    }

    private fun failWith(message: String, reason: SessionErrorReason, throwable: Throwable) {
        logger.error(logTag, message, throwable)
        stopCentralTransport()
        safeTransitionTo(
            VerifierSessionState.Complete.Failed(
                SessionError(message = message, reason = reason)
            )
        )
    }

    private fun safeTransitionTo(
        state: VerifierSessionState,
        logMessage: String = "$CANNOT_TRANSITION_TO_STATE $state",
        exceptionWrapper: ((String, Throwable) -> Exception)? = null,
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
