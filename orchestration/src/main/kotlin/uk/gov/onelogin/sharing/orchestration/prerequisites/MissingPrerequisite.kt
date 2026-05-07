package uk.gov.onelogin.sharing.orchestration.prerequisites

import uk.gov.onelogin.sharing.core.Actionable
import uk.gov.onelogin.sharing.core.Recoverable
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

sealed class MissingPrerequisite(
    val prerequisite: Prerequisite,
    recoverable: Recoverable,
    actionable: Actionable<PrerequisiteAction>
) : Recoverable by recoverable,
    Actionable<PrerequisiteAction> by actionable {

    data class Bluetooth(val state: BluetoothState) :
        MissingPrerequisite(
            prerequisite = Prerequisite.BLUETOOTH,
            recoverable = state,
            actionable = state
        )

    data class Camera(val state: CameraState) :
        MissingPrerequisite(
            prerequisite = Prerequisite.CAMERA,
            recoverable = state,
            actionable = state
        )

    data class Location(val state: LocationState) :
        MissingPrerequisite(
            prerequisite = Prerequisite.LOCATION,
            recoverable = state,
            actionable = state
        )
}
