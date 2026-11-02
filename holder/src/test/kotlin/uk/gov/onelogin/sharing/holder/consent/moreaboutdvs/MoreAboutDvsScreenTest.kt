package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.runtime.Composable
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import kotlin.test.Ignore
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class MoreAboutDvsScreenTest {

    @get:Rule
    val composeTestRule = MoreAboutDvsScreenRule()

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

    @Ignore("Compose hierarchy doesn't load the privacy policy bullet points")
    @Test
    fun `Privacy policy bullet points exist within the UI`() = runTest {
        composeTestRule.run {
            setContent {
                MoreAboutDvsScreen(
                    presentation = presentation
                )
            }

            assertValidPrivacyPolicyBulletPoints()
        }
    }

    private fun verifyUiElements(
        expectedOrganization: String = presentation.organizationName!!,
        content: @Composable () -> Unit
    ) = runTest {

        composeTestRule.run {
            setContent { GdsTheme { content() } }

            assertValidHeader(organizationName = expectedOrganization)
            assertValidBody(organizationName = expectedOrganization)
            assertValidBulletPoints(organizationName = expectedOrganization)
            assertValidPrivacyPolicySubheading(organizationName = expectedOrganization)
            assertValidPrivacyPolicyBody(organizationName = expectedOrganization)
        }
    }
}