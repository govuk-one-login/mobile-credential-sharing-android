package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.Composable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

@RunWith(RobolectricTestRunner::class)
class AgreeToShareCardTest {

    @get:Rule
    val composeTestRule = AgreeToShareCardRule()

    private val organizationName = "Example Organisation."

    private fun card(vararg attributes: Pair<String, Boolean>): ConsentCard = ConsentCard.from(
        ConsentAttributeResolver.toDisplayAttributes(
            attributes.map { (id, retain) -> ConsentAttribute(id, retain) }
        )
    )

    private fun setCard(card: ConsentCard) {
        composeTestRule.setContent { Render(card) }
    }

    @Composable
    private fun Render(card: ConsentCard) {
        AgreeToShareCard(card = card, organizationName = organizationName)
    }

    @Test
    fun `heading shows the attribute count and document name`() {
        setCard(card("family_name" to false, "given_name" to false, "portrait" to false))

        composeTestRule.assertDocumentNameIsDisplayed()
        composeTestRule.assertAttributeCountIsDisplayed(3)
    }

    @Test
    fun `no details kept variant shows the none-kept retention copy`() {
        setCard(card("family_name" to false, "portrait" to false))

        composeTestRule.assertNoneKeptCopyIsDisplayed()
    }

    @Test
    fun `all details kept variant shows the all-kept retention copy`() {
        setCard(card("family_name" to true, "given_name" to true))

        composeTestRule.assertAllKeptCopyIsDisplayed(organizationName)
    }

    @Test
    fun `some details kept variant shows both will-keep and will-not-keep copy`() {
        setCard(card("family_name" to true, "portrait" to false))

        composeTestRule.assertWillKeepCopyIsDisplayed(organizationName)
        composeTestRule.assertWillNotKeepCopyIsDisplayed(organizationName)
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
