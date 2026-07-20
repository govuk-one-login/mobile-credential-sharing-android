package uk.gov.onelogin.sharing.orchestration.holder.session

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.AwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.AwaitingVerifierResolution
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingEstablishment
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@RunWith(TestParameterInjector::class)
class HolderSessionStateTest {

    @Test
    fun `Certain states are cancellable by the User`(
        @TestParameter state: HolderSessionState = testValues(
            AwaitingUserConsent(mockk()),
            AwaitingVerifierResolution,
            ProcessingEstablishment,
            ProcessingResponse
        )
    ) {
        assertTrue {
            state.userCanCancel()
        }
    }

    @Test
    fun `Certain states cannot be cancelled by the User`(
        @TestParameter state: HolderSessionState = testValues(
            HolderSessionState.NotStarted,
            HolderSessionState.Preflight(listOf()),
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement("unit test"),
            HolderSessionState.TerminatingSession,
            HolderSessionState.Complete.Success(),
            HolderSessionState.Complete.Failed(
                SessionError(
                    "",
                    SessionErrorReason.CannotBuildSessionEstablishment
                )
            ),
            HolderSessionState.Complete.Cancelled
        )
    ) {
        assertFalse {
            state.userCanCancel()
        }
    }
}
