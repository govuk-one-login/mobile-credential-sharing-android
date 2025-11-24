package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker

@RunWith(RobolectricTestRunner::class)
class BluetoothPermissionCheckerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var originalSdkInt: Int = Build.VERSION.SDK_INT

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
    fun `returns true when SDK is below S`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.R
        )

        val checker = BluetoothPermissionChecker(context)

        assertTrue(checker.hasPermission())

        verify(exactly = 0) {
            ContextCompat.checkSelfPermission(any(), any())
        }
    }

    @Test
    fun `returns true when permission granted on SDK S or above`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.S
        )

        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } returns PackageManager.PERMISSION_GRANTED

        every {
            ContextCompat.checkSelfPermission(
                context,Manifest.permission.BLUETOOTH_CONNECT
            )
        } returns PackageManager.PERMISSION_GRANTED


        val checker = BluetoothPermissionChecker(context)

        assertTrue(checker.hasPermission())

        verify(exactly = 1) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        }
    }

    @Test
    fun `returns false when permission denied on SDK S or above`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.TIRAMISU
        )

        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } returns PackageManager.PERMISSION_DENIED

        val checker = BluetoothPermissionChecker(context)

        assertFalse(checker.hasPermission())

        verify(exactly = 1) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        }
    }
}
