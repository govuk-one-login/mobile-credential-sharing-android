package uk.gov.onelogin.sharing.core.permission

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import com.google.testing.junit.testparameterinjector.TestParameters
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ActivityPermissionCheckerTest {
    private val activity: Activity = mockk()
    private val permission = Manifest.permission.CAMERA

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
        every {
            ActivityCompat.checkSelfPermission(activity, permission)
        } returns input.grantStatus
        every {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        } returns input.shouldShowRationale

        if (input.wasMarked) {
            markerStore.mark(permission)
        }

        assertThat(
            checker.checkPermissions(permission),
            input.assertion
        )
    }
}
