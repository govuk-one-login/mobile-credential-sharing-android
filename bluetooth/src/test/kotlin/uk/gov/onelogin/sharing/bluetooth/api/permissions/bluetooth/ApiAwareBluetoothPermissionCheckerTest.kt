package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.os.Build
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.Api31BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.truthyBluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.core.permission.StubPermissionChecker

@RunWith(RobolectricTestRunner::class)
class ApiAwareBluetoothPermissionCheckerTest {

    private var originalSdkInt: Int = Build.VERSION.SDK_INT
    private var permissionResult: PermissionChecker.Response = PermissionChecker.Response.Passed

    private val permissionChecker by lazy {
        StubPermissionChecker(
            permissionResult
        )
    }
    private val checker by lazy {
        ApiAwareBluetoothPermissionChecker(permissionChecker)
    }

    @Before
    fun setUp() {
        originalSdkInt = Build.VERSION.SDK_INT
        mockkStatic("androidx.core.content.ContextCompat")
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.core.content.ContextCompat")
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            originalSdkInt
        )
    }

    @Test
    fun `Uses the Truthy implementation when SDK is below S`() = runTest {
        setSdkLevel(Build.VERSION_CODES.R)
        assertThat(
            checker.calculateImplementation(),
            equalTo(truthyBluetoothPermissionChecker)
        )
    }

    @Test
    fun `Uses the API 31 implementation when SDK is S or higher`() = runTest {
        setSdkLevel(Build.VERSION_CODES.S)
        assertThat(
            checker.calculateImplementation(),
            instanceOf(Api31BluetoothPermissionChecker::class.java)
        )
    }

    @Test
    fun `bluetooth checks return true when SDK is below S`() = runTest {
        setSdkLevel(Build.VERSION_CODES.R)
        assertTrue(checker.hasBluetoothPermissions())
    }

    @Test
    fun `returns true when bluetooth permissions granted on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.S)
        assertTrue(checker.hasBluetoothPermissions())
    }

    @Test
    fun `returns false when bluetooth permission denied on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.TIRAMISU)

        permissionResult = PermissionChecker.Response.Missing()
        assertFalse(checker.hasBluetoothPermissions())
    }

    private fun setSdkLevel(sdk: Int) {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            sdk
        )
    }
}
