package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

@ContributesBinding(AppScope::class)
class NoOpReadinessPrerequisiteLayer(private val logger: Logger) : PrerequisiteGateLayer.Readiness {
    override fun checkReadiness(prerequisite: Prerequisite): PrerequisiteResponse.NotReady? =
        null.also {
            logger.debug(
                logTag,
                "Performed $prerequisite readiness check. Response: $it"
            )
        }
}
