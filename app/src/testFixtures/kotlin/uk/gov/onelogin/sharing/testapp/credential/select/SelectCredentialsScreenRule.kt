package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import uk.gov.onelogin.sharing.testapp.CREDENTIAL_ITEM_TAG
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState

class SelectCredentialsScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var selectedCredential: MockCredentialState? = null

    fun assertSelectableCredentialCount(expected: Int) {
        performHolderCredentialMenuClick()
        onAllNodesWithTag(CREDENTIAL_ITEM_TAG, useUnmergedTree = true)
            .assertCountEquals(expected)
        performHolderCredentialMenuClick() // close menu
    }

    fun assertSelectedCredentialEquals(expected: MockCredentialState) = waitUntil {
        expected == selectedCredential
    }

    fun performHolderCredentialMenuClick() = onNodeWithTag(
        "holder_credential_menu",
        useUnmergedTree = true
    ).performClick()

    fun performCredentialClick(credential: MockCredentialState) {
        performHolderCredentialMenuClick()
        onNode(
            hasTestTag(CREDENTIAL_ITEM_TAG) and hasAnyChild(hasText(credential.displayName)),
            useUnmergedTree = true
        ).performClick()
        onNodeWithTag("share_credential_button", useUnmergedTree = true)
            .performClick()
    }

    fun updateMockCredentialState(credential: MockCredentialState) {
        this.selectedCredential = credential
    }
}
