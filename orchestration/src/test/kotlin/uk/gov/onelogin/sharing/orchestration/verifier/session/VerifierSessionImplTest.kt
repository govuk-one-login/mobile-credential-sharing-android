package uk.gov.onelogin.sharing.orchestration.verifier.session

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
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.InvalidVerifierSessionStateTransitions
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.ValidVerifierSessionStateTransitions

@RunWith(TestParameterInjector::class)
class VerifierSessionImplTest {

    private var initialState: VerifierSessionState = VerifierSessionState.NotStarted

    private var validTransitions = validVerifierTransitions

    private val logger = SystemLogger()
    private val session by lazy {
        VerifierSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState),
            transitionMap = validTransitions
        )
    }

    @Test
    @TestParameters(valuesProvider = InvalidVerifierSessionStateTransitions::class)
    fun `IllegalStateExceptions occur when performing invalid transitions`(
        initial: VerifierSessionState,
        transition: VerifierSessionState
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
        @TestParameter(valuesProvider = CompleteVerifierSessionStates::class)
        state: VerifierSessionState
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
    @TestParameters(valuesProvider = ValidVerifierSessionStateTransitions::class)
    fun `Can successfully transition to a valid state`(
        initial: VerifierSessionState,
        transition: VerifierSessionState
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
