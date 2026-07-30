package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metro.createGraphFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier
import uk.gov.onelogin.sharing.ui.impl.di.VerifierUiGraph
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import java.security.cert.X509Certificate
import kotlin.test.assertEquals

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestParameterInjector::class)
class VerifyCredentialTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val trustedRootCertificate: X509Certificate = mockk(relaxed = true)

    private val appGraph by lazy {
        createTestAppGraph()
    }

    private val credentialVerificationGraph by lazy {
        createTestCredentialVerificationGraph(trustedRootCertificate)
    }

    private val verifierGraph by lazy {
        createTestVerifierGraph(appGraph, credentialVerificationGraph)
    }

    private val verifier by lazy {
        FakeCredentialVerifier(
            appGraph = appGraph,
            orchestrator = verifierGraph.verifierOrchestrator()
        )
    }

    @Test
    fun `renders verifier flow`() {
        composeTestRule.setContent {
            VerifyCredential(component = verifier)
        }

        composeTestRule.waitForIdle()
    }

    @Test
    @UiThreadTest
    fun `Close button exists for incomplete journeys`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "not started" to VerifierSessionState.NotStarted,
            "missing prerequisites" to VerifierSessionState.Preflight(emptyList()),
            "Connecting to holder device" to VerifierSessionState.Connecting
        )
    ) = runTest {
        val verifier = FakeCredentialVerifier(
            appGraph = appGraph,
            orchestrator = FakeOrchestrator(
                initialVerifierState = MutableStateFlow(state)
            )
        )

        composeTestRule.run {
            setContent { VerifyCredential(component = verifier) }
            waitForIdle()
            onNodeWithContentDescription("Close").assertExists()
        }
    }

    @Test
    @UiThreadTest
    fun `Close button doesn't exist for complete journeys`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "Cancelled" to VerifierSessionState.Complete.Cancelled,
            "Failed" to VerifierSessionState.Complete.Failed(mockk(relaxed = true)),
            "Success" to VerifierSessionState.Complete.Success(
                DeviceResponse(documents = emptyList())
            )
        )
    ) = runTest {
        val verifier = FakeCredentialVerifier(
            appGraph = appGraph,
            orchestrator = FakeOrchestrator(
                initialVerifierState = MutableStateFlow(state)
            )
        )

        composeTestRule.run {
            setContent { VerifyCredential(component = verifier) }
            waitForIdle()
            onNodeWithContentDescription("Close").assertDoesNotExist()
        }
    }

    @Test
    @UiThreadTest
    fun `Close button on non-confirmable states cancels orchestrator directly`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "Ready to Scan" to VerifierSessionState.ReadyToScan,
            "Preflight" to VerifierSessionState.Preflight(emptyList()),
            "Processing engagement" to VerifierSessionState.ProcessingEngagement
        )
    ) = runTest {
        val orchestrator = FakeOrchestrator(initialVerifierState = MutableStateFlow(state))
        val uiGraph = createGraphFactory<VerifierUiGraph.Factory>()
            .create(appGraph, orchestrator)

        mockkObject(VerifierScanRoute)
        with(VerifierScanRoute) {
            every {
                any<androidx.navigation.NavGraphBuilder>().configureVerifierScannerRoute()
            } answers
                {
                    val builder = firstArg<androidx.navigation.NavGraphBuilder>()
                    builder.composable<VerifierScanRoute> {
                        Box(
                            modifier = androidx.compose.ui.Modifier.testTag("cameraViewfinder")
                        )
                    }
                }
        }

        composeTestRule.run {
            setContent {
                val context = LocalContext.current
                val controller = remember {
                    TestNavHostController(context).apply {
                        navigatorProvider.addNavigator(ComposeNavigator())
                        navigatorProvider.addNavigator(DialogNavigator())
                    }
                }

                VerifyCredential(
                    orchestrator = orchestrator,
                    verifierSessionState = orchestrator.verifierSessionState,
                    viewModelFactory = uiGraph.metroViewModelFactory,
                    controller = controller
                )
            }

            waitForIdle()

            onNodeWithContentDescription("Close").performClick()

            assertEquals(1, orchestrator.cancelCount)
        }
    }

    @Test
    @UiThreadTest
    fun `Close button on confirmable states navigates to dialog`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "Connecting" to VerifierSessionState.Connecting,
            "Verifying" to VerifierSessionState.Verifying
        )
    ) = runTest {
        val orchestrator = FakeOrchestrator(
            initialVerifierState = MutableStateFlow(state)
        )
        val verifier = FakeCredentialVerifier(
            appGraph = appGraph,
            orchestrator = orchestrator
        )

        composeTestRule.run {
            setContent {
                VerifyCredential(component = verifier)
            }
            waitForIdle()
            onNodeWithContentDescription("Close").assertExists()
            composeTestRule.onNodeWithContentDescription("Close").performClick()
            assertEquals(0, orchestrator.cancelCount)
            composeTestRule.onNodeWithText("Deny").isDisplayed()
        }
    }
}
