package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentDocumentSingleton
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentNamespaces
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import kotlin.test.assertEquals

@RunWith(RobolectricTestParameterInjector::class)
class HolderConsentScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderConsentScreenRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private var confirmConsentCount = 0

    private val orchestrator = FakeOrchestrator(
        initialHolderState = holderState,
        onConfirmConsent = { confirmConsentCount++ }
    )

    private val viewModel by lazy {
        HolderConsentViewModel(
            orchestrator = orchestrator,
            analyticsLogger = FakeAnalyticsLogger(),
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    // AC1: attributes displayed without intent-to-retain (all flags false)
    private val attributesWithoutRetain = listOf(
        MdlAttribute.FamilyName,
        MdlAttribute.DocumentNumber,
        MdlAttribute.DrivingPrivileges,
        MdlAttribute.IssueDate,
        MdlAttribute.ExpiryDate,
        MdlAttribute.Portrait,
    ).associateWith { false }

    private val presentationWithoutRetain = createConsentPresentation(
        documents = createConsentDocumentSingleton(
            namespaces = createConsentNamespaces(
                elements = attributesWithoutRetain
            )
        )
    )

    // AC2: attributes displayed with intent-to-retain (all true except portrait)
    private val attributesWithRetain = listOf(
        MdlAttribute.FamilyName,
        MdlAttribute.DocumentNumber,
        MdlAttribute.DrivingPrivileges,
        MdlAttribute.IssueDate,
        MdlAttribute.ExpiryDate,
    ).associateWith {
        true
    } + (MdlAttribute.Portrait to false)

    private val presentationWithRetain = createConsentPresentation(
        documents = createConsentDocumentSingleton(
            namespaces = createConsentNamespaces(
                elements = attributesWithRetain
            )
        )
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

            composeTestRule.run {
                setContent { Render() }
                assertValidUi(presentationWithoutRetain)
                assertNoElementsRetained()
            }
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

            composeTestRule.run {
                setContent { Render() }
                assertValidUi(presentationWithoutRetain)
                assertElementsPartiallyRetained(
                    presentationWithRetain.normalisedOrganizationName
                )
            }
        }

    @Test
    fun `Agree button confirms consent on the orchestrator`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.run {
                setContent { Render() }
                clickAgreeButton(attributeCount = presentationWithoutRetain.attributeCount)
                waitForIdle()
            }

            assertEquals(1, confirmConsentCount)
        }

    @Test
    fun `Cancel button does not cancel the orchestrator as it is out of scope`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.run {
                setContent { Render() }
                clickCancelButton()
                waitForIdle()
            }

            assertEquals(0, orchestrator.cancelCount)
        }

    @Test
    fun `Preview renders without errors`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent {
            HolderConsentScreenPreview()
        }

        composeTestRule.run {
            assertTitleIsDisplayed()
            assertCancelButtonIsDisplayed()
        }
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
                presentation = presentationWithoutRetain.copy(
                    isBusy = input.isBusy
                )
            )
        }

        composeTestRule.run {
            setContent { Render() }
            input.agreeButtonAssertion(this, presentationWithoutRetain.attributeCount, false)
            input.cancelButtonAssertion(this)
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            HolderConsentScreen(
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )
        }
    }

    enum class ButtonEnablementTestData(
        val isBusy: Boolean,
        val agreeButtonAssertion: HolderConsentScreenRule.(Int, Boolean) -> Unit,
        val cancelButtonAssertion: HolderConsentScreenRule.() -> Unit
    ) {
        BUTTONS_ENABLED(
            isBusy = false,
            agreeButtonAssertion = HolderConsentScreenRule::assertAgreeButtonIsEnabled,
            cancelButtonAssertion = HolderConsentScreenRule::assertCancelButtonIsEnabled
        ),
        BUTTONS_DISABLED(
            isBusy = true,
            agreeButtonAssertion = HolderConsentScreenRule::assertAgreeButtonIsNotEnabled,
            cancelButtonAssertion = HolderConsentScreenRule::assertCancelButtonIsNotEnabled
        )
    }
}
