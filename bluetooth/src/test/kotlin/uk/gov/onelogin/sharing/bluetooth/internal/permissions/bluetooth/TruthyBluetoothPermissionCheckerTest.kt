package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

class TruthyBluetoothPermissionCheckerTest {
    @Test
    fun `Always passes central permission checks`() = runTest {
        assertThat(
            TruthyBluetoothPermissionChecker.checkCentralPermissions(),
            equalTo(PermissionChecker.Response.Passed)
        )
    }

    @Test
    fun `Always passes peripheral permission checks`() = runTest {
        assertThat(
            TruthyBluetoothPermissionChecker.checkPeripheralPermissions(),
            equalTo(PermissionChecker.Response.Passed)
        )
    }
}
