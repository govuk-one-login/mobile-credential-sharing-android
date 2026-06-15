package uk.gov.onelogin.sharing.prerequisites.state

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
class BluetoothStateTest {

    @Test
    fun `Some states are unrecoverable`(
        @TestParameter state: BluetoothState = testValues(
            BluetoothState.Restricted,
            BluetoothState.Unsupported
        )
    ) {
        assertThat(
            state,
            isUnrecoverable()
        )
    }

    @Test
    fun `Some states are recoverable`(
        @TestParameter state: BluetoothState = testValues(
            BluetoothState.PermissionDeniedPermanently,
            BluetoothState.PermissionNotGranted,
            BluetoothState.PermissionUndetermined,
            BluetoothState.PoweredOff
        )
    ) {
        assertThat(
            state,
            isRecoverable()
        )
    }

    @Test
    fun `Unrecoverable states have no action`(
        @TestParameter state: BluetoothState = testValues(
            BluetoothState.Restricted,
            BluetoothState.Unsupported
        )
    ) {
        assertThat(
            state.getAction(),
            nullValue()
        )
    }

    @Test
    fun `Recoverable states have an associated action`(
        @TestParameter input: Pair<BluetoothState, Matcher<in PrerequisiteAction>> = testValues(
            BluetoothState.PermissionDeniedPermanently to equalTo(
                PrerequisiteAction.OpenAppPermissions
            ),
            BluetoothState.PermissionNotGranted to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
            ),
            BluetoothState.PermissionUndetermined to instanceOf(
                PrerequisiteAction.RequestPermissions::class.java
            ),
            BluetoothState.PoweredOff to equalTo(
                PrerequisiteAction.EnableBluetooth
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
