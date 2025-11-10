package uk.gov.onelogin.sharing.bluetooth.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
class BluetoothPermissionCheckerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `returns true when SDK is S or below`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.S
        )

        val checker = BluetoothPermissionChecker(context)
        assertTrue(checker.hasPermission())
    }

    @Test
    fun `returns true when permission granted and SDK after S`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.R
        )

        mockStatic(ContextCompat::class.java).use {
            it.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            }.thenReturn(PackageManager.PERMISSION_GRANTED)

            val checker = BluetoothPermissionChecker(context)
            assertTrue(checker.hasPermission())
        }
    }

    @Test
    fun `returns false when permission denied and SDK after S`() {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.R
        )

        mockStatic(ContextCompat::class.java).use {
            it.`when`<Int> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            }.thenReturn(PackageManager.PERMISSION_DENIED)

            val checker = BluetoothPermissionChecker(context)
            assertFalse(checker.hasPermission())
        }
    }
}
