package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

data class FakePrerequisiteAuthorizationGate(var result: AuthorizationResponse) :
    PrerequisiteGateLayer.Authorization {
    override fun checkAuthorization(request: AuthorizationRequest): AuthorizationResponse = result
}
