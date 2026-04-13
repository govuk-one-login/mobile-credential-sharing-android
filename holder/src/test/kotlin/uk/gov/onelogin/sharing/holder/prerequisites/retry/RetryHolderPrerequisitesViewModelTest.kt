package uk.gov.onelogin.sharing.holder.prerequisites.retry

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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.orchestration.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from

class RetryHolderPrerequisitesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private var navigationEvents = mutableListOf<NavigationEvent?>()
    private var hasCalledOnComplete = false
    private var hasCalledResolver = false

    private var initialHolderState: HolderSessionState = HolderSessionState.Preflight(
        listOf(
            MissingPrerequisiteV2.Bluetooth(
                BluetoothState.PermissionNotGranted
            )
        )
    ) { hasCalledOnComplete = true }

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                initialHolderState
            )
        )
    }

    private val viewModel by lazy {
        RetryHolderPrerequisitesViewModel(
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

        viewModel.navigationEvent.test {
            assertEquals(
                NavigationEvent.PassedPrerequisites,
                awaitItem()
            )
        }
    }

    @Test
    fun `Obtains prerequisites from the holder orchestrator's session state`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        viewModel.prerequisites.test {
            assertThat(
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
        initialHolderState = HolderSessionState.NotStarted

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
    }

    @Test
    fun `Rechecking non-preflight states performs no action`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        initialHolderState = HolderSessionState.NotStarted

        viewModel.recheckPrerequisites()

        assertFalse { hasCalledOnComplete }
    }
}
