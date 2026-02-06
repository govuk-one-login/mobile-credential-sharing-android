package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.session.holder.data.HolderSessionStatesWithoutTransition
import uk.gov.onelogin.sharing.orchestration.session.holder.data.InvalidHolderSessionStateTransitions
import uk.gov.onelogin.sharing.orchestration.session.holder.data.ValidHolderSessionStateTransitions
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionMatchers.hasCurrentState

@RunWith(TestParameterInjector::class)
class HolderSessionImplTest {

    private var initialState: HolderSessionState = HolderSessionState.NotStarted

    private val stateFlow: MutableStateFlow<HolderSessionState> by lazy {
        MutableStateFlow(initialState)
    }
    private var validTransitions = validHolderTransitions

    private val logger = SystemLogger()
    private val session by lazy {
        HolderSessionImpl(
            logger = logger,
            internalState = stateFlow,
            transitionMap = validTransitions
        )
    }

    @Test
    @TestParameters(valuesProvider = InvalidHolderSessionStateTransitions::class)
    fun `IllegalStateExceptions occur when performing invalid transitions`(
        initial: HolderSessionState,
        transition: HolderSessionState
    ) = runTest {
        initialState = initial
        val exception = assertThrows(IllegalStateException::class.java) {
            session.transitionTo(transition)
        }

        assertThat(
            exception.message,
            equalTo(
                "Current state (${session.currentState.value::class.java.simpleName}) " +
                    "cannot transition to: ${transition::class.java.simpleName}"
            )
        )

        assertThat(
            session,
            hasCurrentState(initial)
        )

        assert(
            "Cannot transition from '${initial::class.java.simpleName}' " +
                "to '${transition::class.java.simpleName}'" in logger
        )
    }

    @Test
    fun `IllegalStateExceptions occur when the current state has no transitions available`(
        @TestParameter(valuesProvider = HolderSessionStatesWithoutTransition::class)
        state: HolderSessionState
    ) = runTest {
        initialState = state
        val exception = assertThrows(IllegalStateException::class.java) {
            session.transitionTo(state)
        }

        assertThat(
            exception.message,
            equalTo(
                "Cannot find applicable transitions for current state: " +
                    state::class.java.simpleName
            )
        )

        assertThat(
            session,
            hasCurrentState(state)
        )

        assert(
            "Cannot transition from '${state::class.java.simpleName}' " +
                "to '${state::class.java.simpleName}'" in logger
        )
    }

    @Test
    @TestParameters(valuesProvider = ValidHolderSessionStateTransitions::class)
    fun `Can successfully transition to a valid state`(
        initial: HolderSessionState,
        transition: HolderSessionState
    ) = runTest {
        initialState = initial
        session.transitionTo(transition)

        assertThat(
            session,
            hasCurrentState(transition)
        )

        assert(
            "Transitioned from '${initial::class.java.simpleName}' to " +
                "'${transition::class.java.simpleName}'" in logger
        )
    }

    @Test
    fun `Resetting the instance brings the session back to 'Not started'`() = runTest {
        val resetLogMessage = "Cleared holder session state"
        initialState = HolderSessionState.ProcessingResponse
        assertThat(
            session,
            hasCurrentState(initialState)
        )

        assert(resetLogMessage !in logger)
        session.reset()

        assertThat(
            session,
            hasCurrentState(HolderSessionState.NotStarted)
        )
        assert(resetLogMessage in logger)
    }
}
