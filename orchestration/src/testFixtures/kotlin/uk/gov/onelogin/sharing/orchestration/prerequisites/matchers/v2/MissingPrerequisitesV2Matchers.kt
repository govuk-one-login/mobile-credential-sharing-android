package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

object MissingPrerequisitesV2Matchers {
    fun hasBluetoothState(expected: BluetoothState) = hasBluetoothState(equalTo(expected))

    fun hasBluetoothState(matcher: Matcher<in BluetoothState>): Matcher<in MissingPrerequisiteV2> =
        HasBluetoothState(matcher)

    fun hasCameraState(expected: CameraState) = hasCameraState(equalTo(expected))

    fun hasCameraState(matcher: Matcher<in CameraState>): Matcher<in MissingPrerequisiteV2> =
        HasCameraState(matcher)

    fun hasLocationState(expected: LocationState) = hasLocationState(equalTo(expected))

    fun hasLocationState(matcher: Matcher<in LocationState>): Matcher<in MissingPrerequisiteV2> =
        HasLocationState(matcher)
}
