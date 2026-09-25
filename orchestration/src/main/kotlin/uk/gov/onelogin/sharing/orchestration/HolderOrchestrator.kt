package uk.gov.onelogin.sharing.orchestration

import androidx.annotation.Keep
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
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
import uk.gov.onelogin.sharing.core.di.ApplicationScope
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.core.sessionTimer.SessionTimer
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestValidationException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.AgeOverNNRequestLimitException
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
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.orchestration.holder.session.AuthenticatedReaderRequestFactory
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
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthentication
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationFailure
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationOutcome

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
    private val sessionTimer: SessionTimer,
    private val trustedReaderCertificates: List<X509Certificate>,
    private val authenticatedReaderRequestFactory: AuthenticatedReaderRequestFactory,
    private val readerAuthentication: ReaderAuthentication
) : Orchestrator.Holder {
    private var transportStateJob: Job? = null
    private val consentInFlight = AtomicBoolean(false)
    private var signingJob: Job? = null
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
            consentInFlight.set(false)
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
        // Guard against re-entry (e.g. double taps). Because signing now suspends on the
        // consumer's local-auth prompt, the session state remains AwaitingUserConsent until
        // execute() completes, so the state check alone cannot prevent concurrent submissions.
        if (!consentInFlight.compareAndSet(false, true)) {
            logger.debug(logTag, "confirmConsent ignored: a submission is already in flight")
            return
        }

        val state = holderSessionState.value
        val context = currentContext
        try {
            assert(state is HolderSessionState.AwaitingUserConsent) {
                "confirmConsent called in an invalid state: $state"
            }
            check(state is HolderSessionState.AwaitingUserConsent)

            val submission = PendingConsent(
                sessionTranscript = checkNotNull(context.sessionTranscriptBytes) {
                    "Missing session transcript"
                },
                deviceRequest = state.request,
                validatedCredential = checkNotNull(context.validatedCredential) {
                    "Missing validated credential"
                },
                filteredIssuerSigned = checkNotNull(context.filteredIssuerSigned) {
                    "Missing filtered issuer signed"
                },
                skDevice = checkNotNull(context.skDevice) { "Missing skDevice" }
            )

            val activeSession = sessionFlow.value
            signingJob = appCoroutineScope.launch {
                signAndRespond(submission, activeSession)
            }
        } catch (e: IllegalStateException) {
            consentInFlight.set(false)
            appCoroutineScope.launch {
                sendTerminationAndFail(e)
            }
        }
    }

    private suspend fun signAndRespond(submission: PendingConsent, activeSession: HolderSession) {
        try {
            val document = confirmConsentUseCase.execute(
                sessionTranscript = submission.sessionTranscript,
                deviceRequest = submission.deviceRequest,
                validatedCredential = submission.validatedCredential,
                filteredIssuerSigned = submission.filteredIssuerSigned
            )

            // The signing prompt can suspend for a long time. If the session was reset (replaced)
            // or terminated/cancelled while signing was in progress, the captured document and
            // skDevice no longer belong to the active session, so the response is discarded.
            if (sessionFlow.value !== activeSession || activeSession.isComplete()) {
                consentInFlight.set(false)
                logger.debug(
                    logTag,
                    "Session changed during signing; discarding stale device response"
                )
                return
            }

            safeTransitionTo(HolderSessionState.ProcessingResponse)
            sendDeviceResponse(document = document, skDevice = submission.skDevice)
        } catch (e: CredentialSigningException.Recoverable) {
            // Neutral outcome: keep the session active on the consent screen.
            // The user can retry, deny, or cancel.
            consentInFlight.set(false)
            logger.debug(logTag, "$SIGNING_CANCELLED ${e.message ?: ""}".trimEnd())
        } catch (e: DeviceSignatureException) {
            handleFatalSigningFailure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            handleFatalSigningFailure(
                DeviceSignatureException(
                    e.message ?: "Unexpected error building or sending device response",
                    e
                )
            )
        }
    }

    private suspend fun sendDeviceResponse(
        document: VerifiableDocument.WithPresentation,
        skDevice: ByteArray
    ) {
        val context = currentContext
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
    }

    private suspend fun handleFatalSigningFailure(exception: DeviceSignatureException) {
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
                finalState = HolderSessionState.Complete.Failed(
                    SessionError(
                        message = SIGNING_FAILED,
                        exception = exception
                    )
                ),
                sessionDataToSend = sessionDataBytes
            )
        } else {
            sendTerminationAndFail(
                IllegalStateException("Missing skDevice during fatal signing termination")
            )
        }
    }

    override fun denyConsent() {
        val state = holderSessionState.value
        val context = currentContext
        try {
            check(state is HolderSessionState.AwaitingUserConsent) {
                "denyConsent called in an invalid state: $state"
            }

            signingJob?.cancel()
            consentInFlight.set(false)

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
        signingJob?.cancel()
        consentInFlight.set(false)
        appCoroutineScope.launch {
            terminateSession(
                finalState = HolderSessionState.Complete.Cancelled,
                sessionDataToSend = null,
                sendEndCommand = true
            )
        }
    }

    override fun reset() {
        signingJob?.cancel()
        consentInFlight.set(false)
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

    @Suppress("LongMethod", "NestedBlockDepth")
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

            // To be removed in: https://govukverify.atlassian.net/browse/DCMAW-23451 (EX2)
            if (trustedReaderCertificates.isEmpty()) {
                // Empty list: only reachable via the deprecated presentCredentialSdk path,
                // which hardcodes emptyList(). Reader authentication is skipped to preserve
                // the existing behaviour. Temporarily populate authenticatedReaderRequest with
                // dummy privacy-policy and organisation name.
                logger.debug(logTag, "Cert list is empty")
                sessionFlow.value.updateSessionContext {
                    it.copy(
                        authenticatedReaderRequest = authenticatedReaderRequestFactory.create(
                            deviceRequest.docRequests.first()
                        )
                    )
                }

                sessionFlow.value.sessionContext.authenticatedReaderRequest?.let {
                    logger.debug(logTag, "privacy policy = ${it.privacyPolicyUrl}")
                    it.readerOrganizationName?.let { orgName ->
                        logger.debug(logTag, "organisation name = $orgName")
                    }
                }
            } else {
                readerAuthentication.let { auth ->
                    val transcript = checkNotNull(currentContext.sessionTranscriptBytes) {
                        "Missing session transcript"
                    }
                    val outcome = auth.authenticateDeviceRequest(
                        deviceRequest = deviceRequest,
                        untaggedSessionTranscriptBytes = transcript,
                        supportedDocumentTypes = listOf(DocumentType.Mdl.value)
                    )

                    when (outcome) {
                        is ReaderAuthenticationOutcome.Success -> {
                            val authReq = outcome.authenticatedReaderRequest
                            logger.debug(
                                logTag,
                                "Reader Authenticated: Org =" +
                                    " ${authReq.readerOrganizationName}, Privacy Policy URL = " +
                                    "${authReq.privacyPolicyUrl}"
                            )
                            sessionFlow.value.updateSessionContext {
                                it.copy(authenticatedReaderRequest = authReq)
                            }
                        }

                        is ReaderAuthenticationOutcome.Unfulfillable -> {
                            logger.error(logTag, "Reader Authentication UNFULFILLABLE")
                            appCoroutineScope.launch {
                                handleNoMatchTermination(
                                    CredentialRequestException("Unfulfillable request")
                                )
                            }
                            return
                        }
                    }
                }
                logger.debug(logTag, "Cert list is not empty")
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
        } catch (e: ReaderAuthenticationFailure) {
            appCoroutineScope.launch {
                handleReaderAuthFailure(e)
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
            val isAgeOverLimit = exception.cause is AgeOverNNRequestLimitException

            val deviceResponseStatus = if (isAgeOverLimit) Status.GENERAL_ERROR else Status.OK

            val sessionDataBytes = holderCryptoService.buildErrorSessionData(
                deviceResponseStatus = deviceResponseStatus,
                sessionDataStatus = SessionDataStatus.SESSION_TERMINATION,
                skDevice = skDevice,
                encryptCounter = context.encryptCounter
            )

            val finalState = if (isAgeOverLimit) {
                HolderSessionState.Complete.Failed(
                    SessionError(
                        message = AgeOverNNRequestLimitException.MESSAGE,
                        reason = SessionErrorReason.AgeOverNNRequestLimit
                    )
                )
            } else {
                HolderSessionState.Complete.Success(
                    HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
                )
            }

            terminateSession(
                finalState = finalState,
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

    private suspend fun handleReaderAuthFailure(failure: ReaderAuthenticationFailure) {
        logger.error(logTag, "Reader Authentication Failed: ${failure.reason}", failure)
        val context = currentContext
        val skDevice = checkNotNull(context.skDevice) {
            "skDevice must be derived before handling reader authentication failure"
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
                    message = "Reader Authentication Failed: ${failure.reason}",
                    exception = failure
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

    /**
     * The validated inputs required to sign and build a device response, captured up front from
     * the session context so the signing coroutine does not re-read mutable session state.
     * Not a `data class`: it holds [ByteArray]s and needs no structural equality.
     */
    private class PendingConsent(
        val sessionTranscript: ByteArray,
        val deviceRequest: DeviceRequest,
        val validatedCredential: ValidatedCredential,
        val filteredIssuerSigned: IssuerSigned,
        val skDevice: ByteArray
    )

    private companion object {
        val INACTIVITY_TIMEOUT = 300.seconds
        const val UNKNOWN_ERROR = "Unknown error"
        const val PORTRAIT_POLICY_VIOLATION =
            "Policy violation: DeviceRequest does not request portrait attribute"
        const val UNRECOGNISED_MESSAGE =
            "Sequencing violation: inbound message is not a recognised type"
        const val STOPPING_BLE_ADVERTISING = "Stopping BLE advertising"
        const val SIGNING_CANCELLED =
            "Local authentication cancelled during signing; remaining on consent screen."
        const val SIGNING_FAILED = "Unable to sign the credential"
    }
}
