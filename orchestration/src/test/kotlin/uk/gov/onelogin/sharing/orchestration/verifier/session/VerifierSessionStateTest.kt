package uk.gov.onelogin.sharing.orchestration.verifier.session

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnverifiableDocument
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isFailed
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError

@RunWith(TestParameterInjector::class)
class VerifierSessionStateTest {

    /**
     * DCMAW-20270: AC7: When the session transitions to [Failed], the [VerificationError]
     * reason from [VerificationResult.Failure] is available on the Failed state.
     */
    @Test
    fun `Failures can contain Document verification errors`(
        @TestParameter error: VerificationError,
    ) {
        val failure = Failed(
            SessionError(
                "This is a unit test",
                SessionErrorReason.UnverifiableDocument(error)
            )
        )

        assertThat(
            failure,
            isFailed(
                hasReason(
                    isUnverifiableDocument(error)
                )
            )
        )
    }

    @Test
    fun `Certain states are user cancellable`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "Connecting" to VerifierSessionState.Connecting,
            "Processing engagement" to VerifierSessionState.ProcessingEngagement,
            "Verifying" to VerifierSessionState.Verifying,
        ),
    ) = runTest {
        assertTrue {
            state.userCanCancel()
        }
    }

    @Test
    fun `Certain states aren't user cancellable`(
        @TestParameter state: VerifierSessionState = namedTestValues(
            "Cancelled" to VerifierSessionState.Complete.Cancelled,
            "Failed" to Failed(mockk(relaxed = true)),
            "Missing prerequisites" to VerifierSessionState.Preflight(emptyList()),
            "Not started" to VerifierSessionState.NotStarted,
            "QR Scanner" to VerifierSessionState.ReadyToScan,
            "Success" to VerifierSessionState.Complete.Success(mockk(relaxed = true)),
            "Terminating Session" to VerifierSessionState.TerminatingSession,
        ),
    ) = runTest {
        assertFalse {
            state.userCanCancel()
        }
    }
}
