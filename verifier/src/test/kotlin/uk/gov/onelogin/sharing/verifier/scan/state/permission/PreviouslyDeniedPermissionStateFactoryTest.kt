package uk.gov.onelogin.sharing.verifier.scan.state.permission

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyDeniedPermission
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyGrantedPermission

@RunWith(AndroidJUnit4::class)
class PreviouslyDeniedPermissionStateFactoryTest {

    private val flow = MutableStateFlow(false)
    private val state: PreviouslyDeniedPermissionState.Complete =
        PreviouslyDeniedPermissionState.Complete.from(flow)

    @Test
    fun initialState() {
        assertThat(
            state,
            hasPreviouslyGrantedPermission()
        )
    }

    @Test
    fun canUpdatePreviouslyDeniedPermission() = runTest {
        backgroundScope.launch {
            flow.collect {}
        }

        state.update(true)

        assertThat(
            state,
            hasPreviouslyDeniedPermission()
        )
    }
}
