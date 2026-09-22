package uk.gov.onelogin.sharing.verification.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ReaderAuthenticationFailureTest {

    @Test
    fun `ReaderAuthenticationFailure stores reason and message correctly`() {
        val failure = ReaderAuthenticationFailure(ReaderAuthenticationReason.READER_AUTH_MISSING)

        assertEquals(ReaderAuthenticationReason.READER_AUTH_MISSING, failure.reason)
        assertEquals(
            "Reader Authentication failed with reason: READER_AUTH_MISSING",
            failure.message
        )
        assertNull(failure.cause)
    }

    @Test
    fun `ReaderAuthenticationFailure stores cause when provided`() {
        val cause = IllegalArgumentException("Root cause")
        val failure = ReaderAuthenticationFailure(
            reason = ReaderAuthenticationReason.INVALID_READER_SIGNATURE,
            cause = cause
        )

        assertEquals(ReaderAuthenticationReason.INVALID_READER_SIGNATURE, failure.reason)
        assertEquals(cause, failure.cause)
    }

    @Test
    fun `ReaderAuthenticationFailure is throwable`() {
        assertThrows(ReaderAuthenticationFailure::class.java) {
            throw ReaderAuthenticationFailure(
                ReaderAuthenticationReason.UNTRUSTED_READER_CERTIFICATE
            )
        }
    }
}
