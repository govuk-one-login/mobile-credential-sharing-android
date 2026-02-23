package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.core.app.ApplicationProvider
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
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.TruthyBluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.permissions.ContextCompatStaticMocks

@RunWith(RobolectricTestRunner::class)
class ApiAwareBluetoothPermissionCheckerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val contextMocks = ContextCompatStaticMocks(context)
    private var originalSdkInt: Int = Build.VERSION.SDK_INT
    private val checker by lazy {
        ApiAwareBluetoothPermissionChecker(context)
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
            equalTo(TruthyBluetoothPermissionChecker),
        )
    }

    @Test
    fun `Uses the API 31 implementation when SDK is S or higher`() = runTest {
        setSdkLevel(Build.VERSION_CODES.S)
        assertThat(
            checker.calculateImplementation(),
            instanceOf(Api31BluetoothPermissionChecker::class.java),
        )
    }

    @Test
    fun `central checks return true when SDK is below S`() = runTest {
        setSdkLevel(Build.VERSION_CODES.R)
        assertTrue(checker.hasCentralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions(expectedInteractions = 0)
    }

    @Test
    fun `peripheral checks return true when SDK is below S`() = runTest {
        setSdkLevel(Build.VERSION_CODES.R)
        assertTrue(checker.hasPeripheralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions(expectedInteractions = 0)
    }

    @Test
    fun `returns true when central permission granted on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.S)

        contextMocks.stubAllPermissions(PackageManager.PERMISSION_GRANTED)
        assertTrue(checker.hasCentralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns false when central permission denied on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.TIRAMISU)

        contextMocks.stubAllPermissions(PackageManager.PERMISSION_DENIED)
        assertFalse(checker.hasCentralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns true when peripheral permission granted on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.S)

        contextMocks.stubAllPermissions(PackageManager.PERMISSION_GRANTED)
        assertTrue(checker.hasPeripheralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    @Test
    fun `returns false when peripheral permission denied on SDK S or above`() = runTest {
        setSdkLevel(Build.VERSION_CODES.TIRAMISU)

        contextMocks.stubAllPermissions(PackageManager.PERMISSION_DENIED)
        assertFalse(checker.hasPeripheralPermissions())
        contextMocks.verifyCheckSelfPermissionInteractions()
    }

    private fun setSdkLevel(sdk: Int) {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            sdk,
        )
    }
}
