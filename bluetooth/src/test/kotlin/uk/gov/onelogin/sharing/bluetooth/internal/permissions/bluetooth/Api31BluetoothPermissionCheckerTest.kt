package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import android.Manifest
import android.os.Build
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response
import uk.gov.onelogin.sharing.core.permission.StubPermissionChecker

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [Build.VERSION_CODES.S]
)
class Api31BluetoothPermissionCheckerTest {
    private var permissionResult: Response = Response.Passed

    private val permissionChecker by lazy {
        StubPermissionChecker(
            permissionResult
        )
    }
    private val checker by lazy {
        Api31BluetoothPermissionChecker(permissionChecker)
    }

    @Test
    fun `returns true when peripheral permissions granted`() = runTest {
        assertTrue(checker.hasPeripheralPermissions())
    }

    @Test
    fun `returns false when peripheral permissions denied`() = runTest {
        permissionResult = Response.Missing(Manifest.permission.BLUETOOTH_ADVERTISE)

        assertFalse(checker.hasPeripheralPermissions())
    }

    @Test
    fun `returns true when central permissions granted`() = runTest {
        assertTrue(checker.hasCentralPermissions())
    }

    @Test
    fun `returns false when central permissions denied`() = runTest {
        permissionResult = Response.Missing(Manifest.permission.BLUETOOTH_ADVERTISE)
        assertFalse(checker.hasCentralPermissions())
    }

    @Test
    fun `Performing a peripheral check exposes missing permissions`() = runTest {
        permissionResult = Response.Missing(Manifest.permission.BLUETOOTH_ADVERTISE)

        val result = checker.checkPeripheralPermissions()

        assertThat(
            result,
            instanceOf(Response.Missing::class.java)
        )

        assertThat(
            result as Response.Missing,
            contains(Manifest.permission.BLUETOOTH_ADVERTISE)
        )
    }

    @Test
    fun `Performing a central check exposes missing permissions`() = runTest {
        permissionResult = Response.Missing(Manifest.permission.BLUETOOTH_SCAN)

        val result = checker.checkCentralPermissions()

        assertThat(
            result,
            instanceOf(Response.Missing::class.java)
        )

        assertThat(
            result as Response.Missing,
            contains(Manifest.permission.BLUETOOTH_SCAN)
        )
    }
}
