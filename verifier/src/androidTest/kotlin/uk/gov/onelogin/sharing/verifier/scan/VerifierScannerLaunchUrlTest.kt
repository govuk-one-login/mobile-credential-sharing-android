package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifierScannerLaunchUrlTest {

    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    @Test
    fun customTabIntentLaunchesWhenUriIsAvailable() = runTest {
        composeTestRule.run {
            val model = VerifierScannerViewModel()
            val uri = "https://this.is.an.instrumentation.test".toUri()
            model.update(uri)
            render(model)
            assertIntentLaunched(uri)
        }
    }
}
