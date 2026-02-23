package uk.gov.onelogin.sharing.orchestration.holder.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPeripheralPermissionChecker
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate

@ContributesBinding(
    AppScope::class,
    binding = binding<PrerequisiteGate.Permissions<HolderSessionState>>()
)
class HolderPermissionPrerequisiteGate(
    permissionChecker: BluetoothPeripheralPermissionChecker
) : PrerequisiteGate.Permissions<HolderSessionState>,
    BluetoothPeripheralPermissionChecker by permissionChecker {

    override fun checkPermissions(): PermissionCheckerResult = checkPeripheralPermissions()
}
