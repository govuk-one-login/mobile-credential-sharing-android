package uk.gov.onelogin.sharing.orchestration.holder.session

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.HolderSessionContextStub.holderSessionContextStub
import uk.gov.onelogin.sharing.orchestration.holder.session.data.InvalidHolderSessionStateTransitions
import uk.gov.onelogin.sharing.orchestration.holder.session.data.ValidHolderSessionStateTransitions
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers

@RunWith(TestParameterInjector::class)
class HolderSessionImplTest {

    private var initialState: HolderSessionState = HolderSessionState.NotStarted

    private var validTransitions = validHolderTransitions

    private val logger = SystemLogger()
    private val session by lazy {
        HolderSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState),
            transitionMap = validTransitions,
            initialContext = holderSessionContextStub
        )
    }

    @Test
    @TestParameters(valuesProvider = InvalidHolderSessionStateTransitions::class)
    fun `IllegalStateExceptions occur when performing invalid transitions`(
        initial: HolderSessionState,
        transition: HolderSessionState
    ) = runTest {
        initialState = initial
        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            session.transitionTo(transition)
        }

        MatcherAssert.assertThat(
            exception.message,
            CoreMatchers.equalTo(
                "Current state (${session.currentState.value::class.java.simpleName}) " +
                    "cannot transition to: ${transition::class.java.simpleName}"
            )
        )

        MatcherAssert.assertThat(
            session,
            StateContainerMatchers.hasCurrentState(initial)
        )

        assert("Cannot complete transition" in logger)
    }

    @Test
    fun `IllegalStateExceptions occur when the current state has no transitions available`(
        @TestParameter(valuesProvider = CompleteHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialState = state
        val exception = Assert.assertThrows(IllegalStateException::class.java) {
            session.transitionTo(state)
        }

        MatcherAssert.assertThat(
            exception.message,
            CoreMatchers.equalTo(
                "Cannot find applicable transitions for current state: " +
                    state::class.java.simpleName
            )
        )

        MatcherAssert.assertThat(
            session,
            StateContainerMatchers.hasCurrentState(state)
        )

        assert("Cannot complete transition" in logger)
    }

    @Test
    @TestParameters(valuesProvider = ValidHolderSessionStateTransitions::class)
    fun `Can successfully transition to a valid state`(
        initial: HolderSessionState,
        transition: HolderSessionState
    ) = runTest {
        initialState = initial
        session.transitionTo(transition)

        MatcherAssert.assertThat(
            session,
            StateContainerMatchers.hasCurrentState(transition)
        )

        assert(
            "Transitioned from '${initial::class.java.simpleName}' to " +
                "'${transition::class.java.simpleName}'" in logger
        )
    }
}
