package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onAllNodesWithRole
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState

class SelectCredentialsScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var selectedCredential: MockCredentialState? = null

    fun assertSelectableCredentialCount(expected: Int) = onAllNodesWithRole(Role.Button)
        .assertCountEquals(expected)

    fun assertSelectedCredentialEquals(expected: MockCredentialState) = waitUntil {
        expected == selectedCredential
    }

    fun performCredentialClick(credential: MockCredentialState) = onNodeWithText(
        credential.displayName,
        useUnmergedTree = true
    ).performClick()

    fun updateMockCredentialState(credential: MockCredentialState) {
        this.selectedCredential = credential
    }
}
