package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

@ContributesBinding(AppScope::class)
class NoOpReadinessPrerequisiteLayer : PrerequisiteGateLayer.Readiness {
    override fun checkReadiness(prerequisite: Prerequisite): PrerequisiteResponse.NotReady? = null
}
