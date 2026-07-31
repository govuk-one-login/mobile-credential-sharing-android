package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.annotation.UiThreadTest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.mockk
import java.security.cert.X509Certificate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier

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
            "Failed" to VerifierSessionState.Complete.Failed(
                SessionError(
                    "This is a UI test",
                    Exception()
                )
            ),
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
}
