package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.StateRestorationTester
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.FakePeripheralBluetoothTransport
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.usecases.FakeDecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.HolderOrchestrator
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.data.HolderSessionContextStub.holderSessionContextStub
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate

@RunWith(RobolectricTestParameterInjector::class)
class HolderPrerequisitesScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderPrerequisitesScreenRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private var orchestrator: Orchestrator.Holder = FakeOrchestrator(
        initialHolderState = holderState
    )

    private val viewModel by lazy {
        HolderPrerequisitesViewModel(orchestrator = orchestrator)
    }

    @Test
    @TestParameters(valuesProvider = HolderPrerequisitesScreenStates::class)
    fun `Progress indicator text changes based on session state`(
        state: HolderSessionState,
        composeTestRuleAssertion: HolderPrerequisitesScreenRule.() -> Unit
    ) = runTest(dispatcherRule.testDispatcher) {
        holderState.update { state }

        composeTestRule.setContent {
            Render()
        }

        composeTestRuleAssertion(composeTestRule)
    }

    @Test
    @TestParameters(valuesProvider = HolderPrerequisitesScreenStates::class)
    fun `Preview follows the same display as the main composable screen`(
        state: HolderSessionState,
        composeTestRuleAssertion: HolderPrerequisitesScreenRule.() -> Unit
    ) = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent {
            RenderPreview(state)
        }

        composeTestRuleAssertion(composeTestRule)
    }

    @Test
    @TestParameters(valuesProvider = HolderPrerequisitesScreenHandlers::class)
    fun `Certain session states call composable lambdas`(
        state: HolderSessionState,
        handlerAssertion: HolderPrerequisitesScreenRule.() -> Boolean
    ) = runTest(dispatcherRule.testDispatcher) {
        holderState.update { state }

        composeTestRule.setContent {
            Render()
        }

        assertTrue {
            handlerAssertion(composeTestRule)
        }
    }

    @Test
    fun `State restoration occurs without issues`() = runTest(dispatcherRule.testDispatcher) {
        val tester = StateRestorationTester(composeTestRule)
        val logger = SystemLogger()
        orchestrator = HolderOrchestrator(
            appCoroutineScope = backgroundScope,
            credentialProvider = FakeCredentialProvider(),
            decryptDeviceRequestUseCase = FakeDecryptDeviceRequestUseCase(),
            logger = logger,
            sessionFactory = {
                HolderSessionImpl(
                    logger = logger,
                    internalState = holderState,
                    initialContext = holderSessionContextStub
                )
            },
            peripheralBluetoothTransport = FakePeripheralBluetoothTransport(),
            prerequisiteGate = StubPrerequisiteGate()
        )

        tester.setContent {
            Render()
        }

        composeTestRule.assertPresentingEngagementTextIsDisplayed()
        assertTrue { composeTestRule.hasPresentedEngagement }

        tester.emulateSavedInstanceStateRestore()

        composeTestRule.assertPresentingEngagementTextIsDisplayed()
    }

    /**
     * Suppresses 'compose:vm-forwarding-check' as this is for testing purposes.
     */
    @Composable
    private fun Render() {
        HolderPrerequisitesScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            onHandlePreflight = {
                composeTestRule.updateHasHandledPreflight()
            },
            onPresentEngagement = {
                composeTestRule.updateHasPresentedEngagement()
            }
        )
    }

    @Composable
    private fun RenderPreview(state: HolderSessionState) {
        HolderPrerequisitesScreenPreview(state = state)
    }
}
