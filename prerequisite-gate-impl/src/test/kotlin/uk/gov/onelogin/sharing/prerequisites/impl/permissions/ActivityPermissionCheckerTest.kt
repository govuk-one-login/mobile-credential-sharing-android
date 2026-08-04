package uk.gov.onelogin.sharing.prerequisites.impl.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.prerequisites.permissions.ListPermissionStore
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckResultMatchers.hasPermission
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckerParameters
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckerParameters.Companion.markedDeniedPermissionInputs
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckerParameters.Companion.unmarkedDeniedPermissionInputs

@RunWith(RobolectricTestParameterInjector::class)
class ActivityPermissionCheckerTest {
    private val activity: Activity = mockk()

    private val markerStore by lazy {
        ListPermissionStore()
    }

    private val checker by lazy {
        ActivityPermissionChecker(
            activity,
            markerStore = markerStore
        )
    }

    @Before
    fun setUp() {
        mockkStatic(
            ActivityCompat::class
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(
            ActivityCompat::class
        )
    }

    @TestParameters(valuesProvider = PermissionCheckerParameters::class)
    @Test
    fun `Permission checker logic`(input: PermissionCheckerParameters.Input) = runTest {
        processSetUp(input)

        assertThat(
            checker.checkPermissions(input.permission),
            input.assertion
        )
    }

    @Test
    fun `Failure results contain the relevant permission`(
        @TestParameter input: PermissionCheckerParameters.Input = testValuesIn(
            markedDeniedPermissionInputs + unmarkedDeniedPermissionInputs
        )
    ) = runTest {
        processSetUp(input)

        assertThat(
            checker.checkPermissions(input.permission),
            contains(hasPermission(input.permission))
        )
    }

    @Test
    fun `checkPermissions does not mark permissions in the store`() = runTest {
        val permission = Manifest.permission.CAMERA
        every {
            ActivityCompat.checkSelfPermission(activity, permission)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        } returns false

        checker.checkPermissions(permission)

        assertFalse(permission in markerStore)
    }

    @Test
    fun `markAsRequested marks permissions in the store`() = runTest {
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH
        )

        checker.markAsRequested(permissions)

        permissions.forEach { permission ->
            assertTrue(permission in markerStore)
        }
    }

    private fun processSetUp(input: PermissionCheckerParameters.Input) {
        every {
            ActivityCompat.checkSelfPermission(activity, input.permission)
        } returns input.grantStatus
        every {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, input.permission)
        } returns input.shouldShowRationale

        if (input.wasMarked) {
            markerStore.mark(input.permission)
        }
    }
}
