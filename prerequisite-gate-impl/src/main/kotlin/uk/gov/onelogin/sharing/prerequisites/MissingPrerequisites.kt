package uk.gov.onelogin.sharing.prerequisites

import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.state.LocationState

object MissingPrerequisites {
    data class Bluetooth(val state: BluetoothState) :
        MissingPrerequisite,
        Recoverable by state,
        Actionable<PrerequisiteAction> by state {

        override val prerequisite: Prerequisite = Prerequisite.BLUETOOTH
    }

    data class Camera(val state: CameraState) :
        MissingPrerequisite,
        Recoverable by state,
        Actionable<PrerequisiteAction> by state {

        override val prerequisite: Prerequisite = Prerequisite.CAMERA
    }

    data class Location(val state: LocationState) :
        MissingPrerequisite,
        Recoverable by state,
        Actionable<PrerequisiteAction> by state {

        override val prerequisite: Prerequisite = Prerequisite.LOCATION
    }
}
