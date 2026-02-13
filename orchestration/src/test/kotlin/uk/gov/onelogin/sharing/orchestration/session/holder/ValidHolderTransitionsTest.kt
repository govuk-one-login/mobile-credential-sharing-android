package uk.gov.onelogin.sharing.orchestration.session.holder

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
import uk.gov.onelogin.sharing.orchestration.session.holder.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.data.TransitionableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.session.holder.data.ValidHolderSessionStateTransitions

@RunWith(TestParameterInjector::class)
class ValidHolderTransitionsTest {
    @Test
    fun `There are a fixed number of available transitions`() = runTest {
        val transitionTotal = validHolderTransitions.values.sumOf { it.size }

        assertThat(
            "The total number of transitions don't match! " +
                "Is there a need to update the transition state table?",
            transitionTotal,
            equalTo(ValidHolderSessionStateTransitions.inputs.size)
        )
    }

    @Test
    fun `There are a fixed number of states that can transition`() = runTest {
        val transitionableStateCount = validHolderTransitions.keys.size

        assertThat(
            "The number of session states that can transition doesn't match! " +
                "Is there a need to update the transitionable state table?",
            transitionableStateCount,
            equalTo(TransitionableHolderSessionStates.inputs.size)
        )
    }

    @Test
    fun `Certain states can transition`(
        @TestParameter(valuesProvider = TransitionableHolderSessionStates::class)
        state: KClass<out HolderSessionState>
    ) = runTest {
        assertThat(
            "There should have been available transitions for the provided state: " +
                state.simpleName,
            validHolderTransitions[state],
            notNullValue()
        )
    }

    @Test
    fun `Completed states cannot transition`(
        @TestParameter(valuesProvider = CompleteHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        assertThat(
            "There should be no available transitions for the provided state: " +
                state::class.java.simpleName,
            validHolderTransitions[state::class],
            nullValue()
        )
    }
}
