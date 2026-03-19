package uk.gov.onelogin.sharing.holder.prerequisites

import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted

class HolderPrerequisitesViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val orchestrator = FakeOrchestrator()

    private val viewModel by lazy {
        HolderPrerequisitesViewModel(
            dispatcher = dispatcherRule.testDispatcher,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Starts orchestrator on initialisation`() = runTest(dispatcherRule.testDispatcher) {
        assertThat(
            orchestrator.startCount,
            equalTo(0)
        )

        viewModel.holderSessionState.test {
            assertThat(
                awaitItem(),
                isNotStarted()
            )
        }

        assertThat(
            orchestrator.startCount,
            equalTo(1)
        )
    }
}
