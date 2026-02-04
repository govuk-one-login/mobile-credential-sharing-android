package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionMatchers.hasCurrentState

class HolderSessionImplTest {

    private var mutableState: MutableStateFlow<HolderSessionState> =
        MutableStateFlow(HolderSessionState.NotStarted)
    private var validTransitions = HolderSessionImpl.validTransitions

    private val session by lazy {
        HolderSessionImpl(
            internalState = mutableState,
            transitionMap = validTransitions,
        )
    }

    @Test
    fun `IllegalStateExceptions occur when performing invalid transitions`() = runTest {
        val transition = HolderSessionState.Complete.Success(DeviceResponse)
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
    }

    @Test
    fun `IllegalStateExceptions occur when the current state has no transitions available`() =
        runTest {
            validTransitions = mapOf()
            val exception = assertThrows(IllegalStateException::class.java) {
                session.transitionTo(HolderSessionState.Initialising)
            }

            assertThat(
                exception.message,
                equalTo(
                    "Cannot find applicable transitions for current state: NotStarted"
                ),
            )
        }

    @Test
    fun `Can successfully transition to a valid state`() = runTest {
        session.transitionTo(HolderSessionState.Complete.Cancelled)

        assertThat(
            session,
            hasCurrentState(HolderSessionState.Complete.Cancelled)
        )
    }
}
