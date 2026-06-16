package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestParameterInjector::class)
class VerifierPrerequisitesScreenTest {
    @get:Rule
    val composeTestRule = VerifyPrerequisitesScreenRule(createComposeRule())

    @Test
    fun `Calls lambdas based on VerifierSessionState`() = runTest {
        composeTestRule.run {
            setContent {
                VerifierPrerequisitesScreen()
            }

            assertCircularProgressIndicatorIsDisplayed()
        }
    }
}
