package uk.gov.onelogin.sharing.prerequisites

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.equalTo
import org.hamcrest.TypeSafeMatcher
import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.state.LocationState

object MissingPrerequisitesMatchers {
    fun hasBluetoothState(
        expected: BluetoothState,
    ): Matcher<in MissingPrerequisite> = hasBluetoothState(equalTo(expected))

    fun hasBluetoothState(
        matcher: Matcher<in BluetoothState>,
    ): Matcher<in MissingPrerequisite> = MissingPrerequisitesMatcher(matcher) {
        (it as? MissingPrerequisites.Bluetooth)?.state
    }

    fun hasCameraState(
        expected: CameraState,
    ) = hasCameraState(equalTo(expected))

    fun hasCameraState(
        matcher: Matcher<in CameraState>,
    ): Matcher<in MissingPrerequisite> = MissingPrerequisitesMatcher(matcher) {
        (it as? MissingPrerequisites.Camera)?.state
    }

    fun hasLocationState(
        expected: LocationState,
    ) = hasLocationState(equalTo(expected))

    fun hasLocationState(
        matcher: Matcher<in LocationState>,
    ): Matcher<in MissingPrerequisite> = MissingPrerequisitesMatcher(matcher) {
        (it as? MissingPrerequisites.Location)?.state
    }

    private class MissingPrerequisitesMatcher<Type>(
        private val matcher: Matcher<in Type>,
        private val transformer: (MissingPrerequisite?) -> Type?,
    ) : TypeSafeMatcher<MissingPrerequisite>() {
        override fun describeTo(description: Description?) = matcher.describeTo(description)
        override fun describeMismatchSafely(
            item: MissingPrerequisite?,
            mismatchDescription: Description?,
        ) = matcher.describeMismatch(transformer(item), mismatchDescription)

        override fun matchesSafely(item: MissingPrerequisite?): Boolean = matcher.matches(
            transformer(item)
        )
    }
}