package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

@ContributesBinding(AppScope::class)
class NoOpCapabilityPrerequisiteLayer(private val logger: Logger) :
    PrerequisiteGateLayer.Capability {
    override fun checkCapability(prerequisite: Prerequisite): PrerequisiteResponse.Incapable? =
        null.also {
            logger.debug(
                logTag,
                "Performed $prerequisite capability check. Response: $it"
            )
        }
}
