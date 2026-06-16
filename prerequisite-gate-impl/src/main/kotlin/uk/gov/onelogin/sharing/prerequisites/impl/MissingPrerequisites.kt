package uk.gov.onelogin.sharing.prerequisites.impl

import uk.gov.onelogin.sharing.prerequisites.Actionable
import uk.gov.onelogin.sharing.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.Recoverable
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.api.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.api.state.LocationState

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
