package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.MutableState
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiStateMatchers.isLoading
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiStateMatchers.isShowingMoreAboutDvs
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsTestData
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentAttributeResolver
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasCancelCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasConfirmConsentCount
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(TestParameterInjector::class)
class HolderConsentViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val holderState by lazy {
        MutableStateFlow<HolderSessionState>(
            HolderSessionState.NotStarted
        )
    }

    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)

    private val analyticsLogger = FakeAnalyticsLogger()

    private val orchestratorWithConsentTracking = FakeOrchestrator(
        initialHolderState = holderState
    )

    private var showMoreAboutDvs = false

    private val viewModel by lazy {
        HolderConsentViewModel(
            orchestrator = orchestrator,
            analyticsLogger = analyticsLogger,
            dispatcher = dispatcherRule.testDispatcher,
            showMoreAboutDvs = MutableStateFlow(showMoreAboutDvs)
        )
    }

    @Test
    fun `onAccept calls confirmConsent on orchestrator`() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = HolderConsentViewModel(
            orchestrator = orchestratorWithConsentTracking,
            analyticsLogger = analyticsLogger
        )

        viewModel.onAccept().join()

        assertThat(
            orchestratorWithConsentTracking,
            hasConfirmConsentCount(1)
        )
        assertThat(
            analyticsLogger,
            contains(
                Button(
                    text = "Agree and share details",
                    params = walletSharingRequiredParameters
                ).asLegacyEvent()
            )
        )
    }

    @Test
    fun `onDeny causes orchestrator cancellation`() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = HolderConsentViewModel(
            orchestrator = orchestratorWithConsentTracking,
            analyticsLogger = analyticsLogger
        )

        viewModel.onDeny().join()

        assertThat(
            orchestratorWithConsentTracking,
            hasCancelCount(1)
        )
    }

    @Test
    fun `onCancelAndGoBack logs the cancel and go back button`() =
        runTest(dispatcherRule.testDispatcher) {
            viewModel.onCancelAndGoBack().join()

            assertThat(
                analyticsLogger,
                contains(
                    Button(
                        text = "Cancel and go back to documents",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        }

    @Test
    fun `Emits AwaitingUserConsent presentation when orchestrator transitions`() =
        runTest(dispatcherRule.testDispatcher) {
            val documents = listOf(
                ConsentDocument(
                    docType = "org.iso.18013.5.1.mDL",
                    namespaces = listOf(
                        ConsentNamespace(
                            nameSpace = "org.iso.18013.5.1",
                            attributes = listOf(
                                ConsentAttribute(
                                    "family_name",
                                    false,
                                    nameSpace = "org.iso.18013.5.1"
                                )
                            )
                        )
                    )
                )
            )
            val presentation = ConsentPresentation(
                documents = documents,
                organizationName = "Organisation Ltd",
                privacyPolicyUrl = "https://verifier.example/privacy"
            )
            val expected = HolderConsentUiState.Loaded(
                card = ConsentCard.from(
                    ConsentAttributeResolver.toDisplayAttributes(
                        listOf(
                            ConsentAttribute("family_name", false, nameSpace = "org.iso.18013.5.1")
                        )
                    )
                ),
                normalisedOrganizationName = "Organisation Ltd.",
                organizationName = "Organisation Ltd",
                privacyPolicyUrl = "https://verifier.example/privacy"
            )

            viewModel.uiState.test {
                assertThat(
                    awaitItem(),
                    isLoading()
                )

                holderState.value = HolderSessionState.AwaitingUserConsent(
                    request = deviceRequestStub,
                    presentation = presentation
                )

                assertThat(awaitItem(), equalTo(expected))
            }
        }

    @Test
    fun `onReadPrivacyNotice logs the privacy notice button with the organisation name`() =
        runTest(dispatcherRule.testDispatcher) {
            val presentation = ConsentPresentation(
                documents = emptyList(),
                organizationName = "Organisation Ltd",
                privacyPolicyUrl = "https://verifier.example/privacy"
            )
            val expectedState = HolderConsentUiState.Loaded(
                card = null,
                normalisedOrganizationName = "Organisation Ltd.",
                organizationName = "Organisation Ltd",
                privacyPolicyUrl = "https://verifier.example/privacy"
            )

            viewModel.uiState.test {
                assertThat(
                    awaitItem(),
                    isLoading()
                )

                holderState.value = HolderSessionState.AwaitingUserConsent(
                    request = deviceRequestStub,
                    presentation = presentation
                )

                assertThat(awaitItem(), equalTo(expectedState))

                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onReadPrivacyNotice().join()

            assertThat(
                analyticsLogger,
                contains(
                    Button(
                        text = "Read about DVS and their privacy notice",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        }

    @Test
    fun `sendScreenEvent logs a screen view for the consent screen id`() =
        runTest(dispatcherRule.testDispatcher) {
            viewModel.sendReviewWhatYouWillShareScreenEvent("Review what you’ll share").join()

            assertThat(
                analyticsLogger,
                contains(
                    Screen(
                        name = "Review what you’ll share",
                        id = "ac1f2e03-2b4e-4804-8c16-2d18c37b0f9e",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        }

    @Test
    fun `Sends an analytics screen event for the More about DVS screen`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.sendMoreAboutDvsScreenEvent(MoreAboutDvsTestData.SCREEN_NAME).join()

        assertThat(
            analyticsLogger,
            contains(MoreAboutDvsTestData.expectedScreenEvent)
        )
    }

    @Test
    fun `Sends an analytics button event when opening the privacy policy URL`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.onOpenDvsPrivacyPolicy(MoreAboutDvsTestData.PRIVACY_NOTICE_BUTTON_TEXT).join()

        assertThat(
            analyticsLogger,
            contains(MoreAboutDvsTestData.expectedPrivacyPolicyButtonEvent)
        )
    }

    @Test
    fun `Sends an analytics button event when dismissing the More about DVS screen`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.dismissMoreAboutDvsScreen().join()

        assertThat(
            analyticsLogger,
            contains(MoreAboutDvsTestData.expectedDismissButtonEvent)
        )
    }

    @Test
    fun `Showing more about DVS is dependent on the boolean state flow`(
        @TestParameter input: Boolean
    ) = runTest(dispatcherRule.testDispatcher) {
        val presentation = ConsentPresentation(
            documents = emptyList(),
            organizationName = "Organisation Ltd",
            privacyPolicyUrl = "https://verifier.example/privacy"
        )

        showMoreAboutDvs = input
        holderState.value = HolderSessionState.AwaitingUserConsent(
            presentation = presentation,
            request = mockk(relaxed = true)
        )

        viewModel.uiState.test {
            assertThat(
                expectMostRecentItem(),
                isShowingMoreAboutDvs(input)
            )
        }
    }
}
