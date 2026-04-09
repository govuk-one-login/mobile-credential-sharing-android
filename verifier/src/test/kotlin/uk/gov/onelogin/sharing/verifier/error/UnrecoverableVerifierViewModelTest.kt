package uk.gov.onelogin.sharing.verifier.error

import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class UnrecoverableVerifierViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private var initialVerifierState = VerifierSessionState.Complete.Failed(
        SessionError(
            "This is a unit test",
            SessionErrorReason.UnrecoverablePrerequisite()
        )
    )

    private val events = mutableListOf<UnrecoverableVerifierViewModel.NavigationEvent?>()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialVerifierState),
            startCount = 1
        )
    }

    private val viewModel by lazy {
        UnrecoverableVerifierViewModel(
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Exiting the journey resets the orchestrator`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        monitor(viewModel)

        viewModel.exitJourney().join()

        assertThat(
            orchestrator.startCount,
            equalTo(0)
        )

        assertThat(
            events[0],
            equalTo(UnrecoverableVerifierViewModel.NavigationEvent.ExitJourney)
        )
    }

    private fun TestScope.monitor(viewModel: UnrecoverableVerifierViewModel) {
        backgroundScope.launch { viewModel.failureState.collect { } }
        backgroundScope.launch {
            viewModel.navigationEvent.collect { event ->
                events.add(event)
            }
        }
    }
}
