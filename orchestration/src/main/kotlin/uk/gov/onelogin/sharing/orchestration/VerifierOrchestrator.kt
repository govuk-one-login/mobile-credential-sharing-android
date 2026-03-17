package uk.gov.onelogin.sharing.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.completedPrerequisiteChecks
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.createSessionResetMessage
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.recreateSessionOnStartMessage
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotCancelException
import uk.gov.onelogin.sharing.orchestration.exceptions.OrchestratorCannotStartException
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate.Companion.meetsPrerequisites
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSession
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Verifier>())
@SingleIn(AppScope::class)
class VerifierOrchestrator(
    private val logger: Logger,
    private val prerequisiteGate: PrerequisiteGate,
    private val sessionFactory: SessionFactory<VerifierSession>
) : Orchestrator.Verifier {

    private val sessionFlow = MutableStateFlow(sessionFactory.create())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val verifierSessionState = sessionFlow.flatMapLatest { it.currentState }

    override fun start() {
        if (sessionFlow.value.isComplete()) {
            sessionFlow.value = sessionFactory.create().also {
                logger.debug(
                    logTag,
                    recreateSessionOnStartMessage(Orchestrator.Verifier.JOURNEY_NAME)
                )
            }
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
                sessionFlow.value.transitionTo(VerifierSessionState.ReadyToScan)
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
            .let(sessionFlow.value::transitionTo)
    }

    override fun processQrCode(qrCode: BarcodeDataResult) {
        when (qrCode) {
            is BarcodeDataResult.Valid -> sessionFlow.value.transitionTo(
                VerifierSessionState.ProcessingEngagement(
                    qrCode.data
                )
            )

            is BarcodeDataResult.Invalid -> {
                sessionFlow.value.transitionTo(
                    VerifierSessionState.Complete.Failed(
                        SessionError(
                            message = qrCode.data,
                            exception = IllegalArgumentException("Qr Code is an unsupported format")
                        )
                    )
                )
            }

            else -> Unit
        }
    }

    override fun cancel() {
        try {
            println(sessionFlow.value.currentState)
            sessionFlow.value.transitionTo(
                VerifierSessionState.Complete.Cancelled
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
    }

    override fun reset() {
        sessionFlow.value = sessionFactory.create().also {
            logger.debug(
                logTag,
                createSessionResetMessage(Orchestrator.Verifier.JOURNEY_NAME)
            )
        }
    }
}
