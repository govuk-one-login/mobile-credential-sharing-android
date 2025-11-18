package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifierScannerDeniedTest {

    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    @Test
    fun permissionDeniedTextIsShown() = runTest {
        composeTestRule.run {
            render()
            assertPermissionDeniedButtonIsDisplayed()
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun permissionDeniedTextRenderedWithPermissionState() = runTest {
        composeTestRule.run {
            render(permissionState = {
                rememberPermissionState(permission = Manifest.permission.CAMERA)
            })
            assertPermissionDeniedButtonIsDisplayed()
        }
    }
}
