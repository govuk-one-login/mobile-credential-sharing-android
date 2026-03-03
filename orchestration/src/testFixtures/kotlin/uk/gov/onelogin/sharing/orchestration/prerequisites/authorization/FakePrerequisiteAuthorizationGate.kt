package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

data class FakePrerequisiteAuthorizationGate(
    var result: PrerequisiteResponse.Unauthorized? = null
) : PrerequisiteGateLayer.Authorization {
    override fun checkAuthorization(
        prerequisite: Prerequisite
    ): PrerequisiteResponse.Unauthorized? = result
}
