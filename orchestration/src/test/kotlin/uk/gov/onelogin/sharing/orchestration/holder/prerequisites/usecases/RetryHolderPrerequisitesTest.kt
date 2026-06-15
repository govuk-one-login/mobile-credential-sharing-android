package uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases

import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent

class RetryHolderPrerequisitesTest {

    private lateinit var initialHolderState: HolderSessionState

    private val logger = SystemLogger()
    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val navigator by lazy {
        RetryHolderPrerequisites(
            orchestrator = orchestrator,
            logger = logger
        )
    }

    @Test
    fun `Emits unrecoverable event due to unrecoverable bluetooth state`() = runTest {
        initialHolderState = HolderSessionState.Preflight(
            listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.Unsupported)
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
        initialHolderState = HolderSessionState.Preflight(emptyList())

        navigator.events.test {
            assertThat(
                awaitItem(),
                equalTo(NavigationEvent.UnrecoverableError)
            )
        }
    }

    @Test
    fun `Recoverable bluetooth states don't emit navigation events`() = runTest {
        initialHolderState = HolderSessionState.Preflight(
            listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.PermissionNotGranted)
            )
        )
        navigator.events.test { expectNoEvents() }
    }

    @Test
    fun `Presenting engagement states pass prerequisites`() = runTest {
        initialHolderState = HolderSessionState.PresentingEngagement("This is a unit test")

        navigator.events.test {
            assertThat(
                awaitItem(),
                equalTo(NavigationEvent.PassedPrerequisites)
            )
        }
    }

    @Test
    fun `Irrelevant holder session states don't emit navigation events`() = runTest {
        initialHolderState = HolderSessionState.NotStarted

        navigator.events.test { expectNoEvents() }
    }
}
