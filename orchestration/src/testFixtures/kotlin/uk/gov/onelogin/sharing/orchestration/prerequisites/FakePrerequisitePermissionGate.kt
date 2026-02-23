package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.core.permission.PermissionCheckerResult

data class FakePrerequisitePermissionGate<in State : Any>(var result: PermissionCheckerResult) :
    PrerequisiteGate.Permissions<State> {
    override fun checkPermissions(): PermissionCheckerResult = result
}
