package uk.gov.onelogin.sharing.orchestration.session.verifier

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.reflect.KClass
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.session.verifier.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.session.verifier.data.TransitionableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.session.verifier.data.ValidVerifierSessionStateTransitions

@RunWith(TestParameterInjector::class)
class ValidVerifierTransitionsTest {
    @Test
    fun `There are a fixed number of available transitions`() = runTest {
        val transitionTotal = validVerifierTransitions.values.sumOf { it.size }

        assertThat(
            "The total number of transitions don't match! " +
                "Is there a need to update the transition state table?",
            transitionTotal,
            equalTo(ValidVerifierSessionStateTransitions.inputs.size)
        )
    }

    @Test
    fun `There are a fixed number of states that can transition`() = runTest {
        val transitionableStateCount = validVerifierTransitions.keys.size

        assertThat(
            "The number of session states that can transition doesn't match! " +
                "Is there a need to update the transitionable state table?",
            transitionableStateCount,
            equalTo(TransitionableVerifierSessionStates.inputs.size)
        )
    }

    @Test
    fun `Certain states can transition`(
        @TestParameter(valuesProvider = TransitionableVerifierSessionStates::class)
        state: KClass<out VerifierSessionState>
    ) = runTest {
        assertThat(
            "There should have been available transitions for the provided state: " +
                state.simpleName,
            validVerifierTransitions[state],
            notNullValue()
        )
    }

    @Test
    fun `Completed states cannot transition`(
        @TestParameter(valuesProvider = CompleteVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        assertThat(
            "There should be no available transitions for the provided state: " +
                state::class.java.simpleName,
            validVerifierTransitions[state::class],
            nullValue()
        )
    }
}
