package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.zacsweers.metro.createGraphFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    val appGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
        .create(
            applicationContext = ApplicationProvider.getApplicationContext(),
            logger = SystemLogger()
        )

    val holderGraph = createGraphFactory<PresentCredentialGraph.Factory>()
        .create(appGraph = appGraph, credentialProvider = FakeCredentialProvider())

    val verifierGraph = createGraphFactory<VerifyCredentialGraph.Factory>()
        .create(
            appGraph = appGraph,
            verifierConfig = verifierConfigStub
        )

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = MainActivityRule(
        composeTestRule = createAndroidComposeRule<MainActivity>(),
        appGraph = appGraph,
        holderGraph = holderGraph,
        verifierGraph = verifierGraph
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun mainActivityShowsContent() {
        composeRule.assertHolderIsDisplayed()
        composeRule.assertVerifierIsDisplayed()
    }
}
