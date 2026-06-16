package uk.gov.onelogin.sharing.prerequisites.permissions

import android.Manifest
import android.content.pm.PackageManager
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckResultMatchers.isDenied
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckResultMatchers.isPermanentlyDenied
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionCheckResultMatchers.isUndetermined
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionChecker.PermissionCheckResult

class PermissionCheckerParameters : TestParametersValuesProvider() {
    data class Input(
        val name: String,
        val permission: String = Manifest.permission.CAMERA,
        val wasMarked: Boolean = false,
        val grantStatus: Int = PackageManager.PERMISSION_GRANTED,
        val shouldShowRationale: Boolean = false,
        val assertion: Matcher<in Collection<PermissionCheckResult>>
    )

    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf(
            grantedInputs +
                unmarkedDeniedPermissionInputs +
                markedDeniedPermissionInputs
        ).flatMap {
            it
        }.map { input ->
            TestParameters.TestParametersValues.builder()
                .name(input.name)
                .addParameter("input", input)
                .build()
        }

    companion object {
        private val grantedInputs = listOf(
            Input(
                name = "Unmarked granted permissions return empty list",
                assertion = hasSize(0)
            ),
            Input(
                name = "Marking a granted permission has no effect",
                assertion = hasSize(0),
                wasMarked = true
            ),
            Input(
                name = "Showing a rationale has no effect on a granted, unmarked permission",
                assertion = hasSize(0),
                shouldShowRationale = true
            ),
            Input(
                name = "Showing a rationale has no effect on a granted, marked permission",
                assertion = hasSize(0),
                shouldShowRationale = true,
                wasMarked = true
            )
        )

        val markedDeniedPermissionInputs = listOf(
            Input(
                name = "A marked, denied permission without rationale is 'PermanentlyDenied'",
                assertion = contains(isPermanentlyDenied()),
                grantStatus = PackageManager.PERMISSION_DENIED,
                wasMarked = true
            ),
            Input(
                name = "A marked, denied permission with rationale is 'Denied'",
                assertion = contains(isDenied()),
                grantStatus = PackageManager.PERMISSION_DENIED,
                shouldShowRationale = true,
                wasMarked = true
            )
        )

        val unmarkedDeniedPermissionInputs = listOf(
            Input(
                name = "An unmarked, denied permission without rationale is 'Undetermined'",
                assertion = contains(isUndetermined()),
                grantStatus = PackageManager.PERMISSION_DENIED
            ),
            Input(
                name = "Invalid flow: " +
                    "An unmarked, denied permission with rationale is 'Denied'",
                assertion = contains(isDenied()),
                grantStatus = PackageManager.PERMISSION_DENIED,
                shouldShowRationale = true
            )
        )
    }
}
