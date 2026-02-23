package uk.gov.onelogin.sharing.orchestration.verifier.session

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.reflect.KClass
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.TransitionableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.ValidVerifierSessionStateTransitions

@RunWith(TestParameterInjector::class)
class ValidVerifierTransitionsTest {
    @Test
    fun `There are a fixed number of available transitions`() = runTest {
        val transitionTotal = validVerifierTransitions.values.sumOf { it.size }

        MatcherAssert.assertThat(
            "The total number of transitions don't match! " +
                "Is there a need to update the transition state table?",
            transitionTotal,
            CoreMatchers.equalTo(ValidVerifierSessionStateTransitions.Companion.inputs.size)
        )
    }

    @Test
    fun `There are a fixed number of states that can transition`() = runTest {
        val transitionableStateCount = validVerifierTransitions.keys.size

        MatcherAssert.assertThat(
            "The number of session states that can transition doesn't match! " +
                "Is there a need to update the transitionable state table?",
            transitionableStateCount,
            CoreMatchers.equalTo(TransitionableVerifierSessionStates.Companion.inputs.size)
        )
    }

    @Test
    fun `Certain states can transition`(
        @TestParameter(valuesProvider = TransitionableVerifierSessionStates::class)
        state: KClass<out VerifierSessionState>
    ) = runTest {
        MatcherAssert.assertThat(
            "There should have been available transitions for the provided state: " +
                state.simpleName,
            validVerifierTransitions[state],
            CoreMatchers.notNullValue()
        )
    }

    @Test
    fun `Completed states cannot transition`(
        @TestParameter(valuesProvider = CompleteVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        MatcherAssert.assertThat(
            "There should be no available transitions for the provided state: " +
                state::class.java.simpleName,
            validVerifierTransitions[state::class],
            CoreMatchers.nullValue()
        )
    }
}
