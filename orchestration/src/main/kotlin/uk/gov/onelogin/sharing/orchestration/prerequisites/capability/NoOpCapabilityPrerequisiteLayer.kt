package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

@ContributesBinding(AppScope::class)
class NoOpCapabilityPrerequisiteLayer : PrerequisiteGateLayer.Capability {
    override fun checkCapability(
        prerequisite: Prerequisite
    ): PrerequisiteResponse.Incapable? = null
}
