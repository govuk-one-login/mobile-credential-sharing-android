package uk.gov.onelogin.orchestration

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.util.UUID
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
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.security.engagement.GenerateEngagementQrCode

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<Orchestrator.Holder>())
class HolderOrchestrator(
    private val logger: Logger,
    private val sessionFactory: SessionFactory<HolderSession>,
    private val prerequisiteGate: PrerequisiteGate,
    private val qrCodeData: GenerateEngagementQrCode
) : Orchestrator.Holder {

    private var session: HolderSession = sessionFactory.create()

    // this is used to generate the qr, but will also need to be passed to our bluetooth session
    private val stateUUID: UUID = UUID.randomUUID()

    override val holderSessionState: SharedFlow<HolderSessionState> = session.currentState

    override fun start(requiredPermissions: Set<String>) {
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
            is PrerequisiteResponse.Incapable,
            is PrerequisiteResponse.NotReady
            -> Unit

            PrerequisiteResponse.MeetsPrerequisites -> {
                session.transitionTo(HolderSessionState.ReadyToPresent)
                val qrCode = qrCodeData.generateQrCode(stateUUID)
                if (qrCode.isNotEmpty()) {
                    session.transitionTo(
                        HolderSessionState.PresentingEngagement(qrCode)
                    )
                }
            }

            is PrerequisiteResponse.Unauthorized ->
                when (prerequisiteCheck.reason) {
                    is UnauthorizedReason.MissingPermissions -> {
                        session.transitionTo(
                            HolderSessionState.Preflight(Prerequisite.BLUETOOTH)
                        )
                    }
                }
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
    }

    override fun reset() {
        session = sessionFactory.create().also {
            logger.debug(
                logTag,
                createSessionResetMessage(Orchestrator.Holder.JOURNEY_NAME)
            )
        }
    }
}
