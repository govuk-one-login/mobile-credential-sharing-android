package uk.gov.onelogin.sharing.holder.consent

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.ORGANIZATION_NAME
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(RobolectricTestParameterInjector::class)
@Config(qualifiers = "w411dp-h1600dp-xhdpi")
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
        HolderConsentViewModel(
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    // AC1: attributes displayed without intent-to-retain (all flags false)
    private val presentationWithoutRetain = consentPresentation(
        mapOf(
            "family_name" to false,
            "document_number" to false,
            "driving_privileges" to false,
            "issue_date" to false,
            "expiry_date" to false,
            "portrait" to false
        )
    )

    // AC2: attributes displayed with intent-to-retain (all true except portrait)
    private val presentationWithRetain = consentPresentation(
        mapOf(
            "family_name" to true,
            "document_number" to true,
            "driving_privileges" to true,
            "issue_date" to true,
            "expiry_date" to true,
            "portrait" to false
        )
    )

    private fun consentPresentation(elements: Map<String, Boolean>) = ConsentPresentation(
        organizationName = ORGANIZATION_NAME,
        documents = listOf(
            ConsentDocument(
                docType = "org.iso.18013.5.1.mDL",
                namespaces = listOf(
                    ConsentNamespace(
                        nameSpace = "org.iso.18013.5.1",
                        attributes = elements.map { (id, retain) ->
                            ConsentAttribute(elementIdentifier = id, intentToRetain = retain)
                        }
                    )
                )
            )
        )
    )

    private val presentationWithPrivacyPolicy = ConsentPresentation(
        documents = presentationWithoutRetain.documents,
        privacyPolicyUrl = "https://verifier.example/privacy"
    )

    private val presentationWithoutPrivacyPolicy = ConsentPresentation(
        documents = presentationWithoutRetain.documents,
        privacyPolicyUrl = null
    )

    @Test
    fun `AC1 - Displays title, elements without IntentToRetain, and buttons`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertTitleIsDisplayed()
            composeTestRule.assertAcceptButtonIsDisplayed()
            composeTestRule.assertDenyButtonIsDisplayed()
            composeTestRule.assertElementsDisplayed("Last name")
            composeTestRule.assertElementsDisplayed("Licence number")
            composeTestRule.assertElementsDisplayed("Photo")
            composeTestRule.assertElementsDisplayed(
                "These details will only be used for this check. They will not be saved."
            )
        }

    @Test
    fun `AC2 - Displays elements with IntentToRetain flags, portrait is false`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithRetain
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertTitleIsDisplayed()
            composeTestRule.assertElementsDisplayed(
                "$ORGANIZATION_NAME will keep these details as a record of this check:"
            )
            composeTestRule.assertElementsDisplayed("$ORGANIZATION_NAME will not keep:")
            composeTestRule.assertElementsDisplayed("Last name")
            composeTestRule.assertElementsDisplayed("Photo")
        }

    @Test
    fun `Displays the GDS document name in the card heading`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertElementsDisplayed("UK driving licence")
        }

    @Test
    fun `Displays the shared attribute count in the card heading`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertElementsDisplayed("6")
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
    fun `Deny button shows confirmation dialog`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(
                request = DeviceRequestStub.deviceRequestStub,
                presentation = presentationWithoutRetain
            )
        }

        composeTestRule.setContent { Render() }

        composeTestRule.clickDenyButton()
        composeTestRule.waitForIdle()

        composeTestRule.assertDenyDialogIsDisplayed()
    }

    @Test
    fun `Deny dialog confirm button calls denyConsent`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(
                request = DeviceRequestStub.deviceRequestStub,
                presentation = presentationWithoutRetain
            )
        }

        composeTestRule.setContent { Render() }

        composeTestRule.clickDenyButton()
        composeTestRule.waitForIdle()

        composeTestRule.clickDenyDialogConfirm()
        composeTestRule.waitForIdle()

        assertEquals(1, orchestrator.cancelCount)
    }

    @Test
    fun `Deny dialog dismiss button hides dialog`() = runTest(dispatcherRule.testDispatcher) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(
                request = DeviceRequestStub.deviceRequestStub,
                presentation = presentationWithoutRetain
            )
        }

        composeTestRule.setContent { Render() }

        composeTestRule.clickDenyButton()
        composeTestRule.waitForIdle()

        composeTestRule.clickDenyDialogDismiss()
        composeTestRule.waitForIdle()

        composeTestRule.assertDenyDialogIsNotDisplayed()
        composeTestRule.assertTitleIsDisplayed()
    }

    @Test
    fun `Back button behaviour is handled at a higher level`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            lateinit var navController: TestNavHostController
            var backPressDispatcher: OnBackPressedDispatcherOwner? = null

            composeTestRule.setContent {
                val context = LocalContext.current
                backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
                navController = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
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
                backPressDispatcher!!.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()

            assertThat(
                navController.currentDestination?.route,
                equalTo("previous")
            )
        }

    @Test
    fun `Privacy policy link is displayed when a URL is present`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithPrivacyPolicy
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertPrivacyPolicyLinkIsDisplayed()
        }

    @Test
    fun `Privacy policy link is absent when no URL is present`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutPrivacyPolicy
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.assertPrivacyPolicyLinkDoesNotExist()
        }

    @Test
    fun `Tapping the privacy policy link opens the embedded web view`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithPrivacyPolicy
                )
            }

            composeTestRule.setContent { Render() }

            composeTestRule.clickPrivacyPolicyLink()
            composeTestRule.waitForIdle()

            composeTestRule.assertPrivacyPolicyCloseIsDisplayed()
        }

    @Test
    fun `User actions aren't enabled when the view model is busy`(
        @TestParameter input: ButtonEnablementTestData
    ) = runTest(
        dispatcherRule.testDispatcher
    ) {
        holderState.update {
            HolderSessionState.AwaitingUserConsent(
                request = DeviceRequestStub.deviceRequestStub,
                presentation = presentationWithPrivacyPolicy.copy(
                    isBusy = input.isBusy
                )
            )
        }

        composeTestRule.apply {
            setContent { Render() }
            input.acceptButtonAssertion(this)
            input.denyButtonAssertion(this)
        }
    }

    @Composable
    private fun Render() {
        HolderConsentScreen(viewModel = viewModel)
    }

    enum class ButtonEnablementTestData(
        val isBusy: Boolean,
        val acceptButtonAssertion: HolderConsentScreenRule.() -> Unit,
        val denyButtonAssertion: HolderConsentScreenRule.() -> Unit
    ) {
        BUTTONS_ENABLED(
            isBusy = false,
            acceptButtonAssertion = HolderConsentScreenRule::assertAcceptButtonIsEnabled,
            denyButtonAssertion = HolderConsentScreenRule::assertDenyButtonIsEnabled
        ),
        BUTTONS_DISABLED(
            isBusy = true,
            acceptButtonAssertion = HolderConsentScreenRule::assertAcceptButtonIsNotEnabled,
            denyButtonAssertion = HolderConsentScreenRule::assertDenyButtonIsNotEnabled
        )
    }
}
