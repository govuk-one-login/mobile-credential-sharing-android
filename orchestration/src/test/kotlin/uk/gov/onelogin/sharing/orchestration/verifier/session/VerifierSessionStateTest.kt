package uk.gov.onelogin.sharing.orchestration.verifier.session

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
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
    fun `Something`(
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

}