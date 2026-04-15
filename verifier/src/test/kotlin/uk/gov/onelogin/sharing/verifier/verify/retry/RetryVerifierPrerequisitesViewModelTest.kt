package uk.gov.onelogin.sharing.verifier.verify.retry

import app.cash.turbine.test
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.contains
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class RetryVerifierPrerequisitesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private var navigationEvents = mutableListOf<NavigationEvent?>()
    private var hasCalledOnComplete = false
    private var hasCalledResolver = false

    private var initialState: VerifierSessionState = VerifierSessionState.Preflight(
        listOf(
            MissingPrerequisiteV2.Bluetooth(
                BluetoothState.PermissionNotGranted
            )
        )
    ) { hasCalledOnComplete = true }

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(
                initialState
            )
        )
    }

    private val viewModel by lazy {
        RetryVerifierPrerequisitesViewModel(
            navigator = RetryPrerequisitesNavigator.from(navigationEvents.asFlow()),
            orchestrator = orchestrator,
            resolver = {
                hasCalledResolver = true
            },
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Resolving prerequisite actions defer to the resolver`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.resolve(mockk())

        assertTrue { hasCalledResolver }
    }

    @Test
    fun `Navigation events defer to the provided navigator`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        navigationEvents.add(NavigationEvent.PassedPrerequisites)
        viewModel.recheckPrerequisites()

        viewModel.hasRecheckedPrerequisites.test {
            assertTrue { awaitItem() }
        }

        viewModel.navigationEvent.test {
            assertEquals(
                NavigationEvent.PassedPrerequisites,
                awaitItem()
            )
        }
        viewModel.hasRecheckedPrerequisites.test {
            assertFalse { awaitItem() }
        }
    }

    @Test
    fun `Obtains prerequisites from the orchestrator's session state`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.prerequisites.test {
            MatcherAssert.assertThat(
                awaitItem(),
                allOf(
                    hasSize(1),
                    contains(Prerequisite.BLUETOOTH)
                )
            )
        }
    }

    @Test
    fun `Non-preflight states don't provide prerequisites`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialState = VerifierSessionState.NotStarted

        viewModel.prerequisites.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `Rechecking prerequisites defers to the preflight state's lambda`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.recheckPrerequisites()

        assertTrue { hasCalledOnComplete }
        viewModel.hasRecheckedPrerequisites.test {
            assertTrue { awaitItem() }
        }
    }

    @Test
    fun `Rechecking non-preflight states performs no action`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialState = VerifierSessionState.NotStarted

        viewModel.recheckPrerequisites()

        assertFalse { hasCalledOnComplete }
        viewModel.hasRecheckedPrerequisites.test {
            assertTrue { awaitItem() }
        }
    }
}
