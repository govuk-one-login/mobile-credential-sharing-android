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
class CameraStateTest {

    @Test
    fun `Some states are unrecoverable`(
        @TestParameter state: CameraState = testValues(
            CameraState.Restricted,
            CameraState.Unsupported
        )
    ) {
        assertThat(
            state,
            isUnrecoverable()
        )
    }

    @Test
    fun `Some states are recoverable`(
        @TestParameter state: CameraState = testValues(
            CameraState.PermissionDeniedPermanently,
            CameraState.PermissionNotGranted,
            CameraState.PermissionUndetermined
        )
    ) {
        assertThat(
            state,
            isRecoverable()
        )
    }

    @Test
    fun `Unrecoverable states have no action`(
        @TestParameter state: CameraState = testValues(
            CameraState.Restricted,
            CameraState.Unsupported
        )
    ) {
        assertThat(
            state.getAction(),
            nullValue()
        )
    }

    @Test
    fun `Recoverable states have an associated action`(
        @TestParameter input: Pair<CameraState, Matcher<in PrerequisiteAction>> = testValues(
            CameraState.PermissionDeniedPermanently to equalTo(
                PrerequisiteAction.OpenAppPermissions
            ),
            CameraState.PermissionNotGranted to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
            ),
            CameraState.PermissionUndetermined to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
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
