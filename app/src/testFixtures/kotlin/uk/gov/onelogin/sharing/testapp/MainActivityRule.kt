package uk.gov.onelogin.sharing.testapp

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider

class MainActivityRule(
    composeTestRule: ComposeContentTestRule,
    private val holderText: String,
    private val verifierText: String
) : ComposeContentTestRule by composeTestRule {

    var hasStartedHolderJourney: Boolean = false
        private set

    var hasStartedVerifierJourney: Boolean = false
        private set

    constructor(
        composeTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeTestRule = composeTestRule,
        holderText = resources.getString(R.string.holder),
        verifierText = resources.getString(R.string.verifier)
    )

    fun assertHolderIsDisplayed() {
        onNodeWithText(holderText).isDisplayed()
    }

    fun assertVerifierIsDisplayed() {
        onNodeWithText(verifierText).isDisplayed()
    }

    fun openHolder() {
        onNodeWithText(holderText)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    fun openVerifier() {
        onNodeWithText(verifierText)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    fun updateStartHolderJourney(hasStartedJourney: Boolean = true) {
        this.hasStartedHolderJourney = hasStartedJourney
    }

    fun assertHolderJourneyHasStarted() = waitUntil { hasStartedHolderJourney }

    fun updateStartVerifierJourney(hasStartedJourney: Boolean = true) {
        this.hasStartedVerifierJourney = hasStartedJourney
    }

    fun assertVerifierJourneyHasStarted() = waitUntil { hasStartedVerifierJourney }
}
