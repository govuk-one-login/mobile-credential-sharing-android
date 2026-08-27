package uk.gov.onelogin.sharing.orchestration

import androidx.annotation.Keep
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Duration.Companion.seconds
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
import uk.gov.onelogin.sharing.core.sessionTimer.SessionTimer
import uk.gov.onelogin.sharing.cryptoService.scanner.QrParser
import uk.gov.onelogin.sharing.cryptoService.scanner.QrScanResult
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.cryptoService.verifier.SessionEstablishmentException
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoContext
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus.SESSION_TERMINATION
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status as DeviceResponseStatus
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANNOT_TRANSITION_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.completedPrerequisiteChecks
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.createSessionResetMessage
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.recreateSessionOnStartMessage
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason.CannotDecryptDeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason.InvalidBluetoothState
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason.InvalidSessionDataPayload
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason.UnverifiableDocument
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verificationrequest.toItemsRequest
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.SessionTerminator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSession
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteGate
import uk.gov.onelogin.sharing.verification.document.DocumentVerifier
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

private const val INVALID_SESSION_DATA = "Received invalid SessionData instance"

@Keep
@Suppress("LongParameterList", "TooManyFunctions", "UnusedVariable")
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
    private val sessionTerminator: SessionTerminator,
    private val sessionTimer: SessionTimer,
    private val readerAuthCredentialProvider: ReaderAuthCredentialProvider
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

        appCoroutineScope.launch {
            terminateSession(
                centralBluetoothTransport.isBleOpen,
                false,
                VerifierSessionState.Complete.Cancelled,
                sendSessionData = false
            )
        }
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

    private suspend fun handleCentralBluetoothState(bluetoothState: CentralBluetoothState) {
        val verifierState = sessionFlow.value.currentState.value
        if (verifierState.isComplete() ||
            verifierState is VerifierSessionState.TerminatingSession
        ) {
            return
        }

        logger.debug(logTag, "BLE state = $bluetoothState, current internal state = $verifierState")

        when (bluetoothState) {
            is CentralBluetoothState.ConnectionStateStarted -> handleConnectionStateStarted()

            is CentralBluetoothState.Disconnected -> handleDisconnected(
                bluetoothState,
                verifierState
            )

            is CentralBluetoothState.Error -> handleError(bluetoothState, verifierState)

            is CentralBluetoothState.CentralBluetoothEnded -> handleBluetoothEnded(verifierState)

            is CentralBluetoothState.Message -> handleMessage(bluetoothState, verifierState)

            else -> Unit
        }
    }

    private fun handleDisconnected(
        state: CentralBluetoothState.Disconnected,
        currentState: VerifierSessionState
    ) {
        if (currentState is VerifierSessionState.Verifying) {
            logger.debug(logTag, "Ignoring disconnect while verifying")
        } else if (!state.isSessionEnd) {
            failWith(
                "Device ${state.address} disconnected unexpectedly",
                InvalidBluetoothState(
                    BluetoothDisconnectedException(
                        "Bluetooth disconnected unexpectedly",
                        IllegalStateException("Device ${state.address} disconnected unexpectedly")
                    )
                )
            )
        }
    }

    private fun handleError(
        state: CentralBluetoothState.Error,
        verifierState: VerifierSessionState
    ) {
        if (verifierState is VerifierSessionState.Verifying) {
            logger.debug(logTag, "Ignoring transport error while verifying: ${state.reason}")
        } else {
            failWith(
                "Bluetooth error: ${state.reason}",
                InvalidBluetoothState(IllegalStateException("Bluetooth error: ${state.reason}"))
            )
        }
    }

    private fun handleBluetoothEnded(currentState: VerifierSessionState) {
        when (currentState) {
            is VerifierSessionState.Verifying -> {
                logger.debug(logTag, "Ignoring GATT End while verifying")
            }

            is VerifierSessionState.Connecting -> {
                failWith(
                    "GATT End received while connecting",
                    InvalidBluetoothState(
                        IllegalStateException("GATT end received while connecting")
                    )
                )
            }

            else -> {
                appCoroutineScope.launch {
                    terminateSession(
                        bleOpen = centralBluetoothTransport.isBleOpen,
                        receivedTermination = false,
                        finalState = VerifierSessionState.Complete.Cancelled
                    )
                }
            }
        }
    }

    private fun handleMessage(
        state: CentralBluetoothState.Message,
        currentState: VerifierSessionState
    ) {
        if (currentState is VerifierSessionState.Verifying) {
            logger.debug(logTag, "Ignoring unexpected message while verifying")
        } else {
            sessionTimer.reset()
            handleCentralBluetoothStateMessage(state)
        }
    }

    private fun handleCentralBluetoothStateMessage(state: CentralBluetoothState.Message) {
        parseSessionData(state.value)?.let { sessionData ->
            val status = sessionData.status
            val data = sessionData.data

            val isTerminalStatus = status != null && status != SessionDataStatus.OK

            when {
                isMalformedSessionData(sessionData) ->
                    handleInvalidSessionData(INVALID_SESSION_DATA)

                isUnexpectedStatusWithData(sessionData) ->
                    handleUnexpectedStatusWithData(status!!)

                data == null ->
                    handleTerminationWithoutData(isTerminalStatus)

                else -> {
                    logger.debug(logTag, "Deserialized SessionData from bluetooth central Message")
                    decryptAndProcessResponse(data, status == SESSION_TERMINATION)
                }
            }
        }
    }

    private fun parseSessionData(message: ByteArray): SessionData? = runCatching {
        verifierCryptoService.deserializeSessionData(message)
    }.onFailure { throwable ->
        handleInvalidSessionData(INVALID_SESSION_DATA, throwable)
    }.getOrNull()

    private fun handleInvalidSessionData(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            failWith(message, SessionErrorReason.InvalidSessionDataPayload, throwable)
        } else {
            failWith(message, SessionErrorReason.InvalidSessionDataPayload)
        }
    }

    private fun isMalformedSessionData(sessionData: SessionData): Boolean =
        sessionData.data == null && sessionData.status == null

    private fun isUnexpectedStatusWithData(sessionData: SessionData): Boolean =
        sessionData.data != null && sessionData.status != null &&
            sessionData.status != SESSION_TERMINATION &&
            sessionData.status != SessionDataStatus.OK

    private fun handleUnexpectedStatusWithData(status: SessionDataStatus) {
        logger.error(logTag, "Received SessionData with data and error status: $status")
        appCoroutineScope.launch {
            terminateSession(
                bleOpen = centralBluetoothTransport.isBleOpen,
                receivedTermination = true,
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        message = "Received SessionData with data and error status: $status",
                        reason = SessionErrorReason.InvalidSessionDataPayload
                    )
                )
            )
        }
    }

    private fun handleTerminationWithoutData(terminationAlreadyReceived: Boolean) {
        logger.error(logTag, INVALID_SESSION_DATA)
        logger.debug(
            logTag,
            "Received status-only SessionData. Termination received: $terminationAlreadyReceived"
        )

        appCoroutineScope.launch {
            terminateSession(
                bleOpen = centralBluetoothTransport.isBleOpen,
                receivedTermination = terminationAlreadyReceived,
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        message = INVALID_SESSION_DATA,
                        reason = InvalidSessionDataPayload
                    )
                )
            )
        }
    }

    private fun decryptAndProcessResponse(data: ByteArray, holderRequestedTermination: Boolean) {
        safeTransitionTo(VerifierSessionState.Verifying)
        appCoroutineScope.launch {
            val context = sessionFlow.value.cryptoContext ?: return@launch failWith(
                "Missing crypto context when decrypting DeviceResponse",
                SessionErrorReason.MissingCryptoContext
            )

            runCatching {
                verifierCryptoService.decryptDeviceResponse(
                    deviceResponseBytes = data,
                    skDevice = context.skDevice,
                    decryptCounter = context.decryptCounter
                )
            }.onSuccess { deviceResponse ->
                updateDecryptCounter(context)
                evaluateDeviceResponse(deviceResponse, holderRequestedTermination)
            }.onFailure { e ->
                handleDecryptionFailure(holderRequestedTermination, e)
            }
        }
    }

    private fun updateDecryptCounter(context: VerifierCryptoContext) {
        sessionFlow.value.updateCryptoContext {
            context.copy(decryptCounter = context.decryptCounter + 1u)
        }.also {
            val updatedContext = sessionFlow.value.cryptoContext ?: return
            logger.debug(
                logTag,
                "Decrypt counter incremented to: ${updatedContext.decryptCounter}"
            )
        }
    }

    private fun handleDecryptionFailure(holderRequestedTermination: Boolean, throwable: Throwable) {
        appCoroutineScope.launch {
            terminateSession(
                bleOpen = centralBluetoothTransport.isBleOpen,
                holderRequestedTermination,
                VerifierSessionState.Complete.Failed(
                    SessionError(
                        message = "Error decrypting DeviceResponse",
                        reason = CannotDecryptDeviceResponse
                    )
                )
            )
        }
        logger.error(logTag, "Error decrypting DeviceResponse", throwable)
    }

    private fun evaluateDeviceResponse(
        deviceResponse: DeviceResponse,
        receivedTerminationFromHolder: Boolean
    ) {
        val status = deviceResponse.status

        if (!receivedTerminationFromHolder &&
            status != DeviceResponseStatus.OK
        ) {
            appCoroutineScope.launch {
                safeTransitionTo(VerifierSessionState.TerminatingSession)
                terminateSession(
                    bleOpen = centralBluetoothTransport.isBleOpen,
                    receivedTermination = receivedTerminationFromHolder,
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = "DeviceRequest processing error: status ${status.code}",
                            reason = SessionErrorReason.DeviceRequestProcessingError(status.code)
                        )
                    )

                )
            }
            logger.error(logTag, "DeviceRequest processing error: status ${status.code}")
            return
        }

        val documents = deviceResponse.documents
        if (documents.isNullOrEmpty()) {
            appCoroutineScope.launch {
                safeTransitionTo(VerifierSessionState.TerminatingSession)
                terminateSession(
                    bleOpen = centralBluetoothTransport.isBleOpen,
                    receivedTermination = receivedTerminationFromHolder,
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = "Document not returned: status ${status.code}",
                            reason = SessionErrorReason.DocumentNotReturned
                        )
                    )
                )
            }
            logger.error(logTag, "Document not returned: status ${status.code}")
            return
        }

        verifyDocuments(deviceResponse, receivedTerminationFromHolder)
    }

    private fun verifyDocuments(
        deviceResponse: DeviceResponse,
        receivedTerminationStatus: Boolean
    ) {
        try {
            deviceResponse.documents!!.forEach { document ->
                documentVerifier.verifyDocument(
                    document,
                    sessionFlow.value.cryptoContext?.sessionTranscriptBytes
                )
            }
            appCoroutineScope.launch {
                terminateSession(
                    bleOpen = centralBluetoothTransport.isBleOpen,
                    receivedTermination = receivedTerminationStatus,
                    finalState = VerifierSessionState.Complete.Success(deviceResponse)
                )
            }
        } catch (exception: VerificationResult.Failure) {
            appCoroutineScope.launch {
                terminateSession(
                    bleOpen = centralBluetoothTransport.isBleOpen,
                    receivedTermination = receivedTerminationStatus,
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = "Failed to verify provided documents (${exception.error})",
                            reason = UnverifiableDocument(exception.error)
                        )
                    )
                )
            }
            logger.error(
                logTag,
                "Failed to verify provided documents (${exception.error})",
                exception
            )
        }
    }

    private suspend fun terminateSession(
        bleOpen: Boolean,
        receivedTermination: Boolean,
        finalState: VerifierSessionState,
        sendSessionData: Boolean = true
    ) {
        val currentState = sessionFlow.value.currentState.value
        if (currentState.isComplete()) return

        val isSessionStarted = currentState.shouldConfirmCancellation() ||
            currentState is VerifierSessionState.TerminatingSession ||
            sessionFlow.value.cryptoContext != null

        if (isSessionStarted) {
            if (currentState !is VerifierSessionState.TerminatingSession) {
                safeTransitionTo(VerifierSessionState.TerminatingSession)
            }

            val context = sessionFlow.value.cryptoContext

            sessionTerminator.terminate(
                serviceUuid = context?.serviceUuid,
                bleOpen = bleOpen,
                holderRequestedTermination = receivedTermination,
                sendSessionData = sendSessionData
            )
        }

        safeTransitionTo(finalState)
    }

    private suspend fun handleConnectionStateStarted() {
        sessionTimer.start(INACTIVITY_TIMEOUT) { cancel() }

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
        itemsRequest: ItemsRequest
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
            sessionTimer.reset()
            if (!sent) error("Failed to send SessionEstablishment message")
        }
    }

    private fun failWith(message: String, reason: SessionErrorReason) =
        failWith(message, reason, IllegalStateException(message))

    private fun failWith(message: String, reason: SessionErrorReason, throwable: Throwable) {
        logger.error(logTag, message, throwable)
        appCoroutineScope.launch {
            terminateSession(
                centralBluetoothTransport.isBleOpen,
                false,
                VerifierSessionState.Complete.Failed(
                    SessionError(message = message, reason = reason)
                )
            )
        }
    }

    private fun safeTransitionTo(
        state: VerifierSessionState,
        logMessage: String = "$CANNOT_TRANSITION_TO_STATE $state",
        exceptionWrapper: ((String, Throwable) -> Exception)? = null
    ) {
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

    private companion object {
        val INACTIVITY_TIMEOUT = 300.seconds
    }
}
