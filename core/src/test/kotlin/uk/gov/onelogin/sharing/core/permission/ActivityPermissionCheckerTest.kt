package uk.gov.onelogin.sharing.core.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ActivityPermissionCheckerTest {
    private val activity: Activity = mockk()
    private val permission = Manifest.permission.CAMERA

    private val checker by lazy {
        ActivityPermissionChecker(activity)
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

    @Test
    fun `Empty lists represent granting all permissions`() = runTest {
        every {
            ActivityCompat.checkSelfPermission(activity, permission)
        } returns PackageManager.PERMISSION_GRANTED

        val result = checker.checkPermissions(permission)
        assertThat(
            result,
            hasSize(0)
        )
    }

    @Test
    fun `Granted permissions aren't included with denied permissions`() = runTest {
        every {
            ActivityCompat.checkSelfPermission(activity, permission)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH)
        } returns PackageManager.PERMISSION_DENIED
        every {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.BLUETOOTH
            )
        } returns true

        val result = checker.checkPermissions(
            permission,
            Manifest.permission.BLUETOOTH
        )

        assertThat(
            result,
            allOf(
                hasSize(1),
                contains(
                    equalTo(
                        PermissionCheckerV2.Denied(
                            Manifest.permission.BLUETOOTH,
                            shouldShowRationale = true
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Denied responses contain rationale values`(@TestParameter shouldShowRationale: Boolean) =
        runTest {
            every {
                ActivityCompat.checkSelfPermission(activity, permission)
            } returns PackageManager.PERMISSION_DENIED
            every {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            } returns shouldShowRationale

            val result = checker.checkPermissions(permission)

            assertThat(
                result,
                allOf(
                    hasSize(1),
                    contains(
                        equalTo(
                            PermissionCheckerV2.Denied(
                                permission,
                                shouldShowRationale
                            )
                        )
                    )
                )
            )
        }
}
