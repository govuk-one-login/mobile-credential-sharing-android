package uk.gov.onelogin.sharing.orchestration.prerequisites.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.performClick
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole

class RetryPrerequisitesContentRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    var hasPassedPrerequisites: Boolean = false
        private set
    var hasUnrecoverableError: Boolean = false
        private set

    fun assertHasPassedPrerequisites() = waitUntil(
        "Hasn't run 'onPassPrerequisites' lambda!"
    ) { hasPassedPrerequisites }

    fun assertHasUnrecoverableError() = waitUntil(
        "Hasn't run 'onUnrecoverableError' lambda!"
    ) { hasUnrecoverableError }

    fun updateHasPassedPrerequisites(hasPassedPrerequisites: Boolean = true) {
        this.hasPassedPrerequisites = hasPassedPrerequisites
    }

    fun updateHasUnrecoverableError(hasUnrecoverableError: Boolean = true) {
        this.hasUnrecoverableError = hasUnrecoverableError
    }

    fun performResolveActionClick() = onNodeWithRole(Role.Companion.Button).performClick()
}
