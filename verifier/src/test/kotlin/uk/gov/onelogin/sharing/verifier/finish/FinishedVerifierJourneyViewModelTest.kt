package uk.gov.onelogin.sharing.verifier.finish

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

class FinishedVerifierJourneyViewModelTest {

    private val orchestrator by lazy {
        FakeOrchestrator()
    }

    private val viewModel by lazy {
        FinishedVerifierJourneyViewModel(
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `Resetting is a delegation from the orchestrator`() = runTest {
        viewModel.reset()

        assertThat(
            orchestrator.resetCount,
            equalTo(1)
        )
    }
}