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
    override fun checkPrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): Map<Prerequisite, PrerequisiteResponse> = prerequisites.associateWith { prerequisite ->
        authorization.checkAuthorization(
            prerequisite
        ) ?: capability.checkCapability(
            prerequisite
        ) ?: readiness.checkReadiness(
            prerequisite
        ) ?: PrerequisiteResponse.MeetsPrerequisites
    }.also {
        logger.debug(
            logTag,
            "Performed prerequisite checks for: $prerequisites"
        )
    }
}
