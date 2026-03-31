package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator.BluetoothPrerequisiteEvaluator
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

@ContributesBinding(AppScope::class)
class PrerequisiteGateV2(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
) : PrerequisiteGate.V2 {
    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>,
    ): List<MissingPrerequisiteV2> = prerequisites.mapNotNull { prerequisite ->
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> handleBluetooth()?.let(MissingPrerequisiteV2::Bluetooth)
            Prerequisite.CAMERA -> handleCamera()?.let(MissingPrerequisiteV2::Camera)
            Prerequisite.LOCATION -> handleLocation()?.let(MissingPrerequisiteV2::Location)
            else -> null
        }
    }

    private fun handleBluetooth(): BluetoothState? {
        return BluetoothPrerequisiteEvaluator(
            context = context,
            permissionChecker = permissionChecker,
        ).evaluate()
    }

    private fun handleCamera(): CameraState? {
        return null
    }

    private fun handleLocation(): LocationState? {
        return null
    }
}

