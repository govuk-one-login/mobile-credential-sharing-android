package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.Composable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.ORGANIZATION_NAME
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.card

@RunWith(RobolectricTestRunner::class)
class AgreeToShareCardTest {

    @get:Rule
    val composeTestRule = AgreeToShareCardRule()

    private fun setCard(card: ConsentCard) {
        composeTestRule.setContent { Render(card) }
    }

    @Composable
    private fun Render(card: ConsentCard) {
        AgreeToShareCard(card = card, organizationName = ORGANIZATION_NAME)
    }

    @Test
    fun `heading shows the attribute count and document name`() {
        setCard(card(AgreeToShareCardSamples.noDetailsKept))

        composeTestRule.assertDocumentNameIsDisplayed()
        composeTestRule.assertAttributeCountIsDisplayed(3)
    }

    @Test
    fun `no details kept variant shows the none-kept retention copy`() {
        setCard(card(AgreeToShareCardSamples.noDetailsKept))

        composeTestRule.assertNoneKeptCopyIsDisplayed()
    }

    @Test
    fun `all details kept variant shows the all-kept retention copy`() {
        setCard(card(AgreeToShareCardSamples.allDetailsKept))

        composeTestRule.assertAllKeptCopyIsDisplayed(ORGANIZATION_NAME)
    }

    @Test
    fun `some details kept variant shows both will-keep and will-not-keep copy`() {
        setCard(card(AgreeToShareCardSamples.someDetailsKept))

        composeTestRule.assertWillKeepCopyIsDisplayed(ORGANIZATION_NAME)
        composeTestRule.assertWillNotKeepCopyIsDisplayed(ORGANIZATION_NAME)
    }

    @Test
    fun `renders attribute labels`() {
        setCard(card("family_name" to false, "document_number" to false))

        composeTestRule.assertAttributeLabelIsDisplayed(R.string.holder_consent_attr_family_name)
        composeTestRule.assertAttributeLabelIsDisplayed(
            R.string.holder_consent_attr_document_number
        )
    }

    @Test
    fun `age_over attribute is rendered with its NN value`() {
        setCard(card("age_over_18" to false))

        composeTestRule.assertAttributeLabelIsDisplayed(R.string.holder_consent_attr_age_over, 18)
    }
}
