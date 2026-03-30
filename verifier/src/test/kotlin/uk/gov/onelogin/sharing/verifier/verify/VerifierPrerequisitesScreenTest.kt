package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierPrerequisitesScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val logger = SystemLogger()

    private var initialSessionState: VerifierSessionState = VerifierSessionState.NotStarted

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialSessionState)
        )
    }
    private val viewModel by lazy {
        VerifierPrerequisitesViewModel(
            logger,
            orchestrator,
            dispatcherRule.testDispatcher
        )
    }

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `Navigates to preflight via lambda`() = runTest(dispatcherRule.testDispatcher) {
        var navigated = false
        initialSessionState = preflightEmptyPermissions

        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                onNavigateToPreflight = { navigated = true },
                onNavigateToScanner = { fail("Shouldn't have navigated to scanner!") }
            )
        }

        composeTestRule.waitUntil { navigated }
        assertTrue(navigated)
    }

    @Test
    fun `Navigates to scanner via lambda`() = runTest(dispatcherRule.testDispatcher) {
        var navigated = false
        initialSessionState = VerifierSessionState.ReadyToScan

        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                onNavigateToPreflight = { fail("Shouldn't have navigated to preflight!") },
                onNavigateToScanner = { navigated = true }
            )
        }

        composeTestRule.waitUntil { navigated }
        assertTrue(navigated)
    }
}
