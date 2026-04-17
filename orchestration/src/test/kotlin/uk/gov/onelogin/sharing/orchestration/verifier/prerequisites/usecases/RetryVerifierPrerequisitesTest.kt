package uk.gov.onelogin.sharing.orchestration.verifier.prerequisites.usecases

import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class RetryVerifierPrerequisitesTest {

    private lateinit var initialState: VerifierSessionState

    private val logger = SystemLogger()
    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialState)
        )
    }

    private val navigator by lazy {
        RetryVerifierPrerequisites(
            orchestrator = orchestrator,
            logger = logger
        )
    }

    @Test
    fun `Emits unrecoverable event due to unrecoverable bluetooth state`() = runTest {
        initialState = VerifierSessionState.Preflight(
            listOf(
                MissingPrerequisite.Bluetooth(BluetoothState.Unsupported)
            )
        )
        navigator.events.test {
            assertThat(
                awaitItem(),
                equalTo(NavigationEvent.UnrecoverableError)
            )
        }
    }

    @Test
    fun `Empty prerequisite lists are considered unrecoverable`() = runTest {
        initialState = VerifierSessionState.Preflight(emptyList())

        navigator.events.test {
            assertThat(
                awaitItem(),
                equalTo(NavigationEvent.UnrecoverableError)
            )
        }
    }

    @Test
    fun `Emits null due to recoverable bluetooth state`() = runTest {
        initialState = VerifierSessionState.Preflight(
            listOf(
                MissingPrerequisite.Bluetooth(BluetoothState.PermissionNotGranted)
            )
        )
        navigator.events.test {
            assertThat(
                awaitItem(),
                nullValue(NavigationEvent::class.java)
            )
        }
    }

    @Test
    fun `Being ready to scan a QR code passes prerequisites`() = runTest {
        initialState = VerifierSessionState.ReadyToScan

        navigator.events.test {
            assertThat(
                awaitItem(),
                equalTo(NavigationEvent.PassedPrerequisites)
            )
        }
    }

    @Test
    fun `Emits null due to irrelevant verifier session state`() = runTest {
        initialState = VerifierSessionState.NotStarted

        navigator.events.test {
            assertThat(
                awaitItem(),
                nullValue(NavigationEvent::class.java)
            )
        }
    }
}
