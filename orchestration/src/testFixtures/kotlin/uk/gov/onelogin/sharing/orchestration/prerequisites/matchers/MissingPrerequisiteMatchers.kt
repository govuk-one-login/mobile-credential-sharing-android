package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import org.hamcrest.Matchers
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

object MissingPrerequisiteMatchers {
    fun hasPrerequisite(expected: Prerequisite): Matcher<in MissingPrerequisite> =
        HasPrerequisite(Matchers.equalTo(expected))

    fun hasBluetoothState(expected: BluetoothState): Matcher<in MissingPrerequisite> =
        HasBluetoothState(Matchers.equalTo(expected))

    fun hasCameraState(expected: CameraState): Matcher<in MissingPrerequisite> =
        HasCameraState(Matchers.equalTo(expected))

    fun hasLocationState(expected: LocationState): Matcher<in MissingPrerequisite> =
        HasLocationState(Matchers.equalTo(expected))
}
