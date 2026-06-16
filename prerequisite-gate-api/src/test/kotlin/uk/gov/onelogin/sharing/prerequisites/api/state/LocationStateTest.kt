package uk.gov.onelogin.sharing.prerequisites.api.state

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.prerequisites.ActionablePrerequisiteMatchers.hasAction
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.RecoverableMatchers.isRecoverable
import uk.gov.onelogin.sharing.prerequisites.RecoverableMatchers.isUnrecoverable

@RunWith(TestParameterInjector::class)
class LocationStateTest {

    @Test
    fun `Some states are unrecoverable`(
        @TestParameter state: LocationState = testValues(
            LocationState.Unsupported
        )
    ) {
        assertThat(
            state,
            isUnrecoverable()
        )
    }

    @Test
    fun `Some states are recoverable`(
        @TestParameter state: LocationState = testValues(
            LocationState.PermissionDeniedPermanently,
            LocationState.PermissionNotGranted,
            LocationState.PermissionUndetermined,
            LocationState.ServicesDisabled
        )
    ) {
        assertThat(
            state,
            isRecoverable()
        )
    }

    @Test
    fun `Unrecoverable states have no action`(
        @TestParameter state: LocationState = testValues(
            LocationState.Unsupported
        )
    ) {
        assertThat(
            state.getAction(),
            nullValue()
        )
    }

    @Test
    fun `Recoverable states have an associated action`(
        @TestParameter input: Pair<LocationState, Matcher<in PrerequisiteAction>> = testValues(
            LocationState.PermissionDeniedPermanently to equalTo(
                PrerequisiteAction.OpenAppPermissions
            ),
            LocationState.PermissionNotGranted to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
            ),
            LocationState.PermissionUndetermined to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
            ),
            LocationState.ServicesDisabled to equalTo(
                PrerequisiteAction.EnableLocationServices
            )
        )
    ) {
        val (state, assertion) = input
        assertThat(
            state,
            hasAction(assertion)
        )
    }
}
