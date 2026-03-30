package uk.gov.onelogin.sharing.orchestration.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(AppScope::class)
class PrerequisiteGateImpl(
    private val authorization: PrerequisiteGateLayer.Authorization,
    private val capability: PrerequisiteGateLayer.Capability,
    private val logger: Logger,
    private val readiness: PrerequisiteGateLayer.Readiness
) : PrerequisiteGate {
    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisite> = prerequisites.mapNotNull { prerequisite ->
        evaluatePrerequisite(prerequisite)?.let { response ->
            MissingPrerequisite(
                prerequisite = prerequisite,
                reason = response
            )
        }
    }.also {
        logger.debug(
            logTag,
            "Performed prerequisite checks for: $prerequisites"
        )
    }

    private fun evaluatePrerequisite(prerequisite: Prerequisite): MissingPrerequisiteReason? =
        authorization.checkAuthorization(
            prerequisite
        ) ?: capability.checkCapability(
            prerequisite
        ) ?: readiness.checkReadiness(
            prerequisite
        )
}
