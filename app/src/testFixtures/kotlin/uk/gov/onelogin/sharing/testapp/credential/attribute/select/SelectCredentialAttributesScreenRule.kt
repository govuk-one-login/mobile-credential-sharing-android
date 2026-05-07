package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSibling
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.testapp.R

class SelectCredentialAttributesScreenRule(
    composeTestRule: ComposeContentTestRule,
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources
) : ComposeContentTestRule by composeTestRule {
    private var confirmedAttributeGroup: AttributeGroup? = null

    fun assertConfirmedAttributeGroupEquals(group: AttributeGroup) = waitUntil {
        group == confirmedAttributeGroup
    }

    fun assertOptionIsSelected(option: VerifierAttributeOption) = onVerifierOptionText(option)
        .onSibling()
        .assertIsSelected()

    fun onVerifierOptionText(option: VerifierAttributeOption) = onNodeWithText(
        option.displayName,
        useUnmergedTree = true
    )

    fun performOptionClick(option: VerifierAttributeOption) = onVerifierOptionText(option)
        .performClick()

    fun performVerifyCredentialClick() = onNodeWithText(
        resources.getString(R.string.verify_credential),
        useUnmergedTree = true
    ).performClick()

    fun updateConfirmedAttributeGroup(group: AttributeGroup) {
        this.confirmedAttributeGroup = group
    }
}
