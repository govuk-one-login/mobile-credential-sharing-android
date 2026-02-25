package uk.gov.onelogin.sharing.orchestration.prerequisites.authorization

import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate

data class FakePrerequisiteAuthorizationGate(var result: AuthorizationResponse) :
    PrerequisiteGate.Authorization {
    override fun checkAuthorization(request: AuthorizationRequest): AuthorizationResponse = result
}
