package uk.gov.onelogin.sharing.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidPermissionCheckerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val contextMocks = ContextCompatStaticMocks(context)

    private val checker by lazy {
        AndroidPermissionChecker(context)
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
    fun `Passes the permission check when provided no permissions to check against`() {
        val result = checker.checkPermissions(emptyList())

        assertThat(
            result,
            equalTo(PermissionChecker.Response.Passed)
        )
    }

    @Test
    fun `ContextCompat granting permissions provide a passed response`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_GRANTED)
        val result = checker.checkPermissions(Manifest.permission.CAMERA)

        assertThat(
            result,
            equalTo(PermissionChecker.Response.Passed)
        )
    }

    @Test
    fun `ContextCompat denying permissions provide a missing response`() = runTest {
        contextMocks.stubAllPermissions(PackageManager.PERMISSION_DENIED)
        val result = checker.checkPermissions(Manifest.permission.CAMERA)

        assertThat(
            result as PermissionChecker.Response.Missing,
            contains(Manifest.permission.CAMERA)
        )
    }

    @Test
    fun `ContextCompat partially granting permissions provide a missing response`() = runTest {
        contextMocks.stubPermission(
            Manifest.permission.CAMERA,
            PackageManager.PERMISSION_GRANTED
        )
        contextMocks.stubPermission(
            Manifest.permission.BLUETOOTH_SCAN,
            PackageManager.PERMISSION_DENIED
        )

        val result = checker.checkPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH_SCAN
        )

        assertThat(
            result as PermissionChecker.Response.Missing,
            contains(Manifest.permission.BLUETOOTH_SCAN)
        )
    }
}
