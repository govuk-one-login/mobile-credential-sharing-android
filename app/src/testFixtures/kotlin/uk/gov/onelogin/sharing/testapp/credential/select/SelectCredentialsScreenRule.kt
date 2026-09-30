package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState

class SelectCredentialsScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var selectedCredential: MockCredentialState? = null

    fun assertSelectableCredentialCount(expected: Int) {
        onNodeWithTag("start_sharing_button").assertExists()
    }

    fun assertSelectedCredentialEquals(expected: MockCredentialState) = waitUntil {
        expected == selectedCredential
    }

    fun performCredentialClick(credential: MockCredentialState) {
        onNodeWithTag("start_sharing_button").performClick()
    }

    fun updateMockCredentialState(credential: MockCredentialState) {
        this.selectedCredential = credential
    }
}
