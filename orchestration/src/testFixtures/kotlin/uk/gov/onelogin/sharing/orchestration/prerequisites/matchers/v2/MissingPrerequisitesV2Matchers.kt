package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

object MissingPrerequisitesV2Matchers {
    fun hasPrerequisite(expected: Prerequisite): Matcher<in MissingPrerequisiteV2> =
        HasPrerequisiteV2(equalTo(expected))

    fun hasBluetoothState(expected: BluetoothState): Matcher<in MissingPrerequisiteV2> =
        HasBluetoothState(equalTo(expected))

    fun hasCameraState(expected: CameraState): Matcher<in MissingPrerequisiteV2> =
        HasCameraState(equalTo(expected))

    fun hasLocationState(expected: LocationState): Matcher<in MissingPrerequisiteV2> =
        HasLocationState(equalTo(expected))
}
