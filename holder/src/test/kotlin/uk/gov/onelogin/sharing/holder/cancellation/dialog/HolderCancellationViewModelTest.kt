package uk.gov.onelogin.sharing.holder.cancellation.dialog

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

class HolderCancellationViewModelTest {

    private val orchestrator = FakeOrchestrator()

    private val viewModel by lazy {
        HolderCancellationViewModel(
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `Journey cancellation defers to the Orchestrator`() = runTest {
        assertThat(
            orchestrator.cancelCount,
            equalTo(0)
        )

        viewModel.cancelJourney().join()

        assertThat(
            orchestrator.cancelCount,
            equalTo(1)
        )
    }

}