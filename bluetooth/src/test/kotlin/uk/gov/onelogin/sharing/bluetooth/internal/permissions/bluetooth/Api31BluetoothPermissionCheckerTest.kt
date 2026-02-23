package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.bluetooth.permissions.ContextCompatStaticMocks
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [Build.VERSION_CODES.S]
)
class Api31BluetoothPermissionCheckerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val contextMocks = ContextCompatStaticMocks(context)

    private val checker by lazy {
        Api31BluetoothPermissionChecker(context)
    }

    @Before
    fun setUp() {
        mockkStatic("androidx.core.content.ContextCompat")
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.core.content.ContextCompat")
    }

    @Test
    fun `returns true when peripheral permissions granted`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_GRANTED)
        assertTrue(checker.hasPeripheralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns false when peripheral permissions denied`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_DENIED)
        assertFalse(checker.hasPeripheralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns true when central permissions granted`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_GRANTED)
        assertTrue(checker.hasCentralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns false when central permissions denied`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_DENIED)
        assertFalse(checker.hasCentralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `Performing a peripheral check exposes missing permissions`() = runTest {
        contextMocks.stubPermission(
            Manifest.permission.BLUETOOTH_CONNECT,
            PackageManager.PERMISSION_GRANTED
        )
        contextMocks.stubPermission(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            PackageManager.PERMISSION_DENIED
        )

        val result = checker.checkPeripheralPermissions()

        assertThat(
            result,
            instanceOf(PermissionChecker.Response.Missing::class.java)
        )

        assertThat(
            result as PermissionChecker.Response.Missing,
            contains(Manifest.permission.BLUETOOTH_ADVERTISE)
        )

        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `Performing a central check exposes missing permissions`() = runTest {
        contextMocks.stubPermission(
            Manifest.permission.BLUETOOTH_CONNECT,
            PackageManager.PERMISSION_GRANTED
        )
        contextMocks.stubPermission(
            Manifest.permission.BLUETOOTH_SCAN,
            PackageManager.PERMISSION_DENIED
        )

        val result = checker.checkCentralPermissions()

        assertThat(
            result,
            instanceOf(PermissionChecker.Response.Missing::class.java)
        )

        assertThat(
            result as PermissionChecker.Response.Missing,
            contains(Manifest.permission.BLUETOOTH_SCAN)
        )

        contextMocks.verifyCheckSelfPermissionInteractions()
    }
}
