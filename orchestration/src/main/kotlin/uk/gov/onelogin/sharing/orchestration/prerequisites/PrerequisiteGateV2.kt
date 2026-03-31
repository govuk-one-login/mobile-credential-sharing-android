package uk.gov.onelogin.sharing.orchestration.prerequisites

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

@ContributesBinding(AppScope::class)
class PrerequisiteGateV2 : PrerequisiteGate.V2 {
    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisiteV2> = prerequisites.mapNotNull { prerequisite ->
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> handleBluetooth()?.let(MissingPrerequisiteV2::Bluetooth)
            Prerequisite.CAMERA -> handleCamera()?.let(MissingPrerequisiteV2::Camera)
            Prerequisite.LOCATION -> handleLocation()?.let(MissingPrerequisiteV2::Location)
            else -> null
        }
    }

    private fun handleBluetooth(): BluetoothState? {
        return null
    }
    private fun handleCamera(): CameraState? {
        return null
    }
    private fun handleLocation(): LocationState? {
        return null
    }
}