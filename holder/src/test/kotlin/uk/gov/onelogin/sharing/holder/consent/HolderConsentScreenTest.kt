package uk.gov.onelogin.sharing.holder.consent

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
class HolderConsentScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderConsentScreenRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)

    private val viewModel by lazy {
        HolderConsentViewModel(orchestrator = orchestrator)
    }

    // DCMAW-16715 AC1: all IntentToRetain flags are false
    private val deviceRequestWithoutRetain = DeviceRequestStub.deviceRequest(
        mapOf(
            "family_name" to false,
            "document_number" to false,
            "driving_privileges" to false,
            "issue_date" to false,
            "expiry_date" to false,
            "portrait" to false
        )
    )

    // DCMAW-16715 AC2: all IntentToRetain flags are true except portrait which is false
    private val deviceRequestWithRetain = DeviceRequestStub.deviceRequest(
        mapOf(
            "family_name" to true,
            "document_number" to true,
            "driving_privileges" to true,
            "issue_date" to true,
            "expiry_date" to true,
            "portrait" to false
        )
    )

    @Test
    fun `AC1 - Displays title, elements without IntentToRetain, and buttons`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertTitleIsDisplayed()
            composeTestRule.assertAcceptButtonIsDisplayed()
            composeTestRule.assertDenyButtonIsDisplayed()
            composeTestRule.assertElementsDisplayed("family_name")
            composeTestRule.assertElementsDisplayed("document_number")
            composeTestRule.assertElementsDisplayed("portrait")
            composeTestRule.assertElementsDisplayed("false")
        }

    @Test
    fun `AC2 - Displays elements with IntentToRetain flags, portrait is false`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(deviceRequestWithRetain)
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertTitleIsDisplayed()
            composeTestRule.assertElementsDisplayed("family_name — Intent to retain: true")
            composeTestRule.assertElementsDisplayed("portrait — Intent to retain: false")
        }

    @Test
    fun `Displays docType from the DeviceRequest`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
        }

        composeTestRule.setContent { Render() }

        composeTestRule.assertElementsDisplayed("org.iso.18013.5.1.mDL")
    }

    @Test
    fun `Displays namespace from the DeviceRequest`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
        }

        composeTestRule.setContent { Render() }

        composeTestRule.assertElementsDisplayed("org.iso.18013.5.1")
    }

    @Test
    fun `Preview renders without errors`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent {
            HolderConsentScreenPreview()
        }

        composeTestRule.assertTitleIsDisplayed()
        composeTestRule.assertAcceptButtonIsDisplayed()
        composeTestRule.assertDenyButtonIsDisplayed()
    }

    @Test
    fun `Back button is disabled and screen remains visible`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
            }

            lateinit var navController: TestNavHostController

            composeTestRule.setContent {
                val context = LocalContext.current
                navController = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }

                NavHost(
                    navController = navController,
                    startDestination = "previous"
                ) {
                    composable("previous") {}
                    composable("consent") {
                        HolderConsentScreen(viewModel = viewModel)
                    }
                }
            }

            composeTestRule.runOnUiThread {
                navController.navigate("consent")
            }
            composeTestRule.waitForIdle()

            composeTestRule.runOnUiThread {
                val activity = navController.context as ComponentActivity
                activity.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()

            assertEquals("consent", navController.currentDestination?.route)
        }

    @Composable
    private fun Render() {
        HolderConsentScreen(viewModel = viewModel)
    }
}
