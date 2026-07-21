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
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingEstablishment
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.TerminatingSession

@RunWith(TestParameterInjector::class)
class HolderSessionStateTest {

    @Test
    fun `Certain states are cancellable by the User`(
        @TestParameter state: HolderSessionState = testValues(
            AwaitingUserConsent(mockk(relaxed = true)),
            AwaitingVerifierResolution,
            ProcessingEstablishment,
            PresentingEngagement("unit test"),
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
            Cancelled,
            Failed(mockk(relaxed = true)),
            NotStarted,
            Preflight(listOf()),
            ReadyToPresent,
            Success(),
            TerminatingSession,
        )
    ) {
        assertFalse {
            state.userCanCancel()
        }
    }
}
