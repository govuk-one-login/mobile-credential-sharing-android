package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

class TruthyBluetoothPermissionCheckerTest {
    @Test
    fun `Always passes central permission checks`() = runTest {
        assertThat(
            truthyBluetoothPermissionChecker.checkBluetoothPermissions(),
            equalTo(PermissionChecker.Response.Passed)
        )
    }

    @Test
    fun `Always passes bluetooth permission checks`() = runTest {
        assertTrue(truthyBluetoothPermissionChecker.hasBluetoothPermissions())
    }
}
