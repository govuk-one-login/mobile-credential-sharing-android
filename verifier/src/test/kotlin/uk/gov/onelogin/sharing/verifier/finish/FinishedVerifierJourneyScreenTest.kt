package uk.gov.onelogin.sharing.verifier.finish

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class FinishedVerifierJourneyScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = FinishedVerifierJourneyScreenRule(createComposeRule())

    private var response: DeviceResponse = DeviceResponseStub.successWithDocuments

    private val orchestrator by lazy {
        FakeOrchestrator()
    }

    private val viewModel by lazy {
        FinishedVerifierJourneyViewModel(
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Exits the journey`(
        @TestParameter action: (
            OnBackPressedDispatcherOwner?,
            FinishedVerifierJourneyScreenRule
        ) -> Unit = namedTestValues(
            "Exit journey button" to { _, rule -> rule.performExitJourneyClick() },
            "Back press" to { dispatcher, _ ->
                dispatcher?.onBackPressedDispatcher?.onBackPressed()
            }
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
            setContent {
                backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                Render()
            }

            action(backPressedDispatcher, composeTestRule)

            assertHasExitedJourney()
            assertHasExitedJourneyCount(equalTo(1))
            assertThat(
                orchestrator.resetCount,
                equalTo(1)
            )
        }
    }

    @Test
    fun `Outputs the Document attributes to the User`() = runTest(dispatcherRule.testDispatcher) {
        response = DeviceResponse(
            documents = listOf(
                DeviceResponseStub.document.copy(
                    issuerSigned = SharingIssuerSigned(
                        nameSpaces = mapOf(
                            NAMESPACE to listOf(
                                encodeItem(1, "given_name", "Jane"),
                                encodeItem(2, "age_over_21", true),
                                encodeItem(3, "portrait", byteArrayOf(1, 2, 3))
                            )
                        ),
                        issuerAuth = byteArrayOf(0x02)
                    )
                )
            )
        )

        composeTestRule.run {
            setContent {
                Render()
            }

            assertDocTypeIsDisplayed(response.documents!![0])
            assertTextIsDisplayed(NAMESPACE)
            assertAttributeIsDisplayed("given_name", "Jane")
            assertAttributeIsDisplayed("age_over_21", "true")
            assertAttributeIsDisplayed("portrait", "<3 bytes>")
        }
    }

    @Test
    fun `Skips attributes that cannot be decoded`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent {
                Render()
            }

            assertDocTypeIsDisplayed(response.documents!![0])
            assertTextIsDisplayed(NAMESPACE)
        }
    }

    private fun encodeItem(digestId: Int, elementIdentifier: String, elementValue: Any): ByteArray {
        val item = CborMapper.default.writeValueAsBytes(
            mapOf(
                "digestID" to digestId,
                "random" to byteArrayOf(0),
                "elementIdentifier" to elementIdentifier,
                "elementValue" to elementValue
            )
        )
        return CborMapper.default.writeValueAsBytes(EmbeddedCbor(item))
    }

    @Composable
    private fun Render() {
        FinishedVerifierJourneyScreen(
            response = response,
            viewModel = viewModel,
            onExitJourney = { composeTestRule.updateHasExitedJourney() }
        )
    }

    private companion object {
        const val NAMESPACE = "org.iso.18013.5.1"
    }
}
