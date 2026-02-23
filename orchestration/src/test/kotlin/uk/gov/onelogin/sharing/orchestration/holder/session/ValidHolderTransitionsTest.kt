package uk.gov.onelogin.sharing.orchestration.holder.session

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.reflect.KClass
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.TransitionableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.ValidHolderSessionStateTransitions

@RunWith(TestParameterInjector::class)
class ValidHolderTransitionsTest {
    @Test
    fun `There are a fixed number of available transitions`() = runTest {
        val transitionTotal = validHolderTransitions.values.sumOf { it.size }

        MatcherAssert.assertThat(
            "The total number of transitions don't match! " +
                "Is there a need to update the transition state table?",
            transitionTotal,
            CoreMatchers.equalTo(ValidHolderSessionStateTransitions.Companion.inputs.size)
        )
    }

    @Test
    fun `There are a fixed number of states that can transition`() = runTest {
        val transitionableStateCount = validHolderTransitions.keys.size

        MatcherAssert.assertThat(
            "The number of session states that can transition doesn't match! " +
                "Is there a need to update the transitionable state table?",
            transitionableStateCount,
            CoreMatchers.equalTo(TransitionableHolderSessionStates.Companion.inputs.size)
        )
    }

    @Test
    fun `Certain states can transition`(
        @TestParameter(valuesProvider = TransitionableHolderSessionStates::class)
        state: KClass<out HolderSessionState>
    ) = runTest {
        MatcherAssert.assertThat(
            "There should have been available transitions for the provided state: " +
                state.simpleName,
            validHolderTransitions[state],
            CoreMatchers.notNullValue()
        )
    }

    @Test
    fun `Completed states cannot transition`(
        @TestParameter(valuesProvider = CompleteHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        MatcherAssert.assertThat(
            "There should be no available transitions for the provided state: " +
                state::class.java.simpleName,
            validHolderTransitions[state::class],
            CoreMatchers.nullValue()
        )
    }
}
