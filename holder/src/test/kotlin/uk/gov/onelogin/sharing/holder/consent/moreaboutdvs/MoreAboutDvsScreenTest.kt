package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class MoreAboutDvsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    private var organizationName: String? = "Stub-DVS Ltd."

    val presentation by lazy {
        createConsentPresentation(
            organizationName = organizationName
        )
    }

    @Test
    fun `UI contains the correct elements`() = verifyUiElements {
        MoreAboutDvsScreen(
            presentation = presentation
        )
    }

    @Test
    fun `Preview contains the correct elements`() = verifyUiElements {
        MoreAboutDvsPreview(
            presentation = presentation
        )
    }

    @Test
    fun `Undefined organizations show 'Unknown' in the title`() = verifyUiElements(
        expectedOrganization = "Unknown"
    ) {
        organizationName = null

        MoreAboutDvsScreen(
            presentation = presentation
        )
    }

    private fun verifyUiElements(
        expectedOrganization: String = presentation.organizationName!!,
        content: @Composable () -> Unit
    ) = runTest {

        composeTestRule.run {
            setContent {
                GdsTheme { content() }
            }

            onNodeWithText(
                resources.getString(
                    R.string.more_about_dvs_title,
                    expectedOrganization
                )
            ).assertExists("Cannot find the title!")
                .assertIsDisplayed()
        }
    }
}