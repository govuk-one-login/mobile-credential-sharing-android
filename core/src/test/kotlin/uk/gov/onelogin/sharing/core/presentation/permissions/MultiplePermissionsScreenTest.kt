package uk.gov.onelogin.sharing.core.presentation.permissions

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionStateExt.toFakePermissionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestRunner::class)
class MultiplePermissionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val deniedUnhappyPathFailure = "The permission shouldn't be required!"
    private val grantedUnhappyPathFailure = "The permission shouldn't have been granted!"
    private val permanentlyDeniedUnhappyPathFailure =
        "The permission shouldn't be permanently denied!"
    private val permission = Manifest.permission.CAMERA
    private val rationaleUnhappyPathFailure = "The rationale shouldn't have been shown!"
    private var hasReachedHappyPath = false

    @Test
    fun grantedPermissionsDeferToOnGrantPermission() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = {
                hasReachedHappyPath = true
            },
            onPermanentlyDenyPermission = { fail(permanentlyDeniedUnhappyPathFailure) },
            onRequirePermission = { _, launchPermission -> fail(deniedUnhappyPathFailure) },
            onShowRationale = { _, launchPermission -> fail(rationaleUnhappyPathFailure) }
        ),
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(PermissionStatus.Granted)
        )
    )

    @Test
    fun secondarilyRequestedPermissionsDeferToShowingTheRationale() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = { fail(grantedUnhappyPathFailure) },
            onPermanentlyDenyPermission = { fail(permanentlyDeniedUnhappyPathFailure) },
            onRequirePermission = { _, launchPermission -> fail(deniedUnhappyPathFailure) },
            onShowRationale = { _, launchPermission ->
                hasReachedHappyPath = true
            }
        ),
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(
                PermissionStatus.Denied(
                    shouldShowRationale = true
                )
            )
        )
    )

    @Test
    fun ungrantedPermissionsDeferToOnRequirePermission() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = { fail(grantedUnhappyPathFailure) },
            onPermanentlyDenyPermission = { fail(permanentlyDeniedUnhappyPathFailure) },
            onRequirePermission = { _, launchPermission ->
                hasReachedHappyPath = true
            },
            onShowRationale = { _, launchPermission -> fail(rationaleUnhappyPathFailure) }
        ),
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(
                PermissionStatus.Denied(
                    shouldShowRationale = false
                )
            )
        )
    )

    @Test
    fun permanentlyDeniedPermissionsRelyOnOuterBooleanFlagAndDeniedStatus() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = { fail(grantedUnhappyPathFailure) },
            onPermanentlyDenyPermission = {
                hasReachedHappyPath = true
            },
            onRequirePermission = { _, launchPermission -> fail(deniedUnhappyPathFailure) },
            onShowRationale = { _, launchPermission -> fail(rationaleUnhappyPathFailure) }

        ),
        hasPreviouslyDeniedPermission = true,
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(
                PermissionStatus.Denied(
                    shouldShowRationale = false
                )
            )
        )
    )

    @Test
    fun callingLaunchPermissionViaRequiredPermission() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = { fail(grantedUnhappyPathFailure) },
            onPermanentlyDenyPermission = { fail(permanentlyDeniedUnhappyPathFailure) },
            onRequirePermission = { _, launchPermission ->
                launchPermission()
            },
            onShowRationale = { _, launchPermission -> fail(rationaleUnhappyPathFailure) }
        ),
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(
                PermissionStatus.Denied(
                    shouldShowRationale = false
                )
            )
        ) {
            hasReachedHappyPath = true
        }
    )

    @Test
    fun callingLaunchPermissionViaPermissionRationale() = performLogicFlow(
        logic = MultiplePermissionsLogic(
            onGrantPermission = { fail(grantedUnhappyPathFailure) },
            onPermanentlyDenyPermission = { fail(permanentlyDeniedUnhappyPathFailure) },
            onRequirePermission = { _, launchPermission -> fail(deniedUnhappyPathFailure) },
            onShowRationale = { _, launchPermission ->
                launchPermission()
            }
        ),
        state = FakeMultiplePermissionsState(
            permission.toFakePermissionState(
                PermissionStatus.Denied(
                    shouldShowRationale = true
                )
            )
        ) {
            hasReachedHappyPath = true
        }
    )

    private fun performLogicFlow(
        logic: MultiplePermissionsLogic,
        state: MultiplePermissionsState,
        hasPreviouslyDeniedPermission: Boolean = false
    ) = runTest {
        composeTestRule.setContent {
            MultiplePermissionsScreen(
                state = state,
                hasPreviouslyRequestedPermission = hasPreviouslyDeniedPermission,
                logic = logic
            )
        }

        assertTrue(hasReachedHappyPath)
    }
}
