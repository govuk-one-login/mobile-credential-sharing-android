package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.Composable
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
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

    private val deviceRequestWithoutRetain = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                ItemsRequest(
                    docType = "org.iso.18013.5.1.mDL",
                    nameSpaces = mapOf(
                        "org.iso.18013.5.1" to mapOf(
                            "family_name" to false,
                            "document_number" to false,
                            "driving_privileges" to false,
                            "issue_date" to false,
                            "expiry_date" to false,
                            "portrait" to false
                        )
                    )
                )
            )
        )
    )

    private val deviceRequestWithRetain = DeviceRequest(
        version = "1.0",
        docRequests = listOf(
            DocRequest(
                ItemsRequest(
                    docType = "org.iso.18013.5.1.mDL",
                    nameSpaces = mapOf(
                        "org.iso.18013.5.1" to mapOf(
                            "family_name" to true,
                            "document_number" to true,
                            "driving_privileges" to true,
                            "issue_date" to true,
                            "expiry_date" to true,
                            "portrait" to false
                        )
                    )
                )
            )
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
            composeTestRule.assertElementIsDisplayed("family_name")
            composeTestRule.assertElementIsDisplayed("document_number")
            composeTestRule.assertElementIsDisplayed("portrait")
            assertTrue { composeTestRule.assertAllElementsDisplayed("false") }
        }

    @Test
    fun `AC2 - Displays elements with IntentToRetain flags, portrait is false`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(deviceRequestWithRetain)
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertTitleIsDisplayed()
            composeTestRule.assertElementIsDisplayed("family_name")
            assertTrue { composeTestRule.assertAllElementsDisplayed("true") }
            assertTrue { composeTestRule.assertAllElementsDisplayed("false") }
        }

    @Test
    fun `Displays docType from the DeviceRequest`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
        }

        composeTestRule.setContent { Render() }

        composeTestRule.assertElementIsDisplayed("org.iso.18013.5.1.mDL")
    }

    @Test
    fun `Displays namespace from the DeviceRequest`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(deviceRequestWithoutRetain)
        }

        composeTestRule.setContent { Render() }

        assertTrue { composeTestRule.assertAllElementsDisplayed("org.iso.18013.5.1") }
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

    @Composable
    private fun Render() {
        HolderConsentScreen(viewModel = viewModel)
    }
}
