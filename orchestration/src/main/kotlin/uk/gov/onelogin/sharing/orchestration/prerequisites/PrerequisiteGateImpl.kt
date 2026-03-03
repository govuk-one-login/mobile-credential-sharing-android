package uk.gov.onelogin.sharing.orchestration.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class PrerequisiteGateImpl(
    private val authorization: PrerequisiteGateLayer.Authorization
) : PrerequisiteGate {
    override fun checkPrerequisites(
        prerequisites: Collection<Prerequisite>
    ): Map<Prerequisite, PrerequisiteResponse> = prerequisites.associateWith { prerequisite ->
        authorization.checkAuthorization(
            prerequisite
        ) ?: PrerequisiteResponse.MeetsPrerequisites
    }
}