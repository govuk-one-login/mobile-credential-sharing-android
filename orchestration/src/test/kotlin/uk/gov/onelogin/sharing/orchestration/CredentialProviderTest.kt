package uk.gov.onelogin.sharing.orchestration

import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialProviderTest {

    private val payload = byteArrayOf(1, 2, 3)
    private val documentId = "doc-id"
    private val signature = byteArrayOf(9, 8, 7)

    /** Legacy provider: overrides only the deprecated [sign], returning raw bytes. */
    private class LegacyProvider(private val onSign: (ByteArray, String) -> ByteArray) :
        CredentialProvider {
        override suspend fun getCredentials(request: CredentialRequest): List<Credential> =
            emptyList()

        @Deprecated("Superseded by signWithResult, which returns a SignResult.")
        override suspend fun sign(payload: ByteArray, documentId: String): ByteArray =
            onSign(payload, documentId)
    }

    /** New provider: overrides only [signWithResult], returning a [SignResult]. */
    private class NewProvider(private val result: SignResult) : CredentialProvider {
        override suspend fun getCredentials(request: CredentialRequest): List<Credential> =
            emptyList()

        override suspend fun signWithResult(payload: ByteArray, documentId: String): SignResult =
            result
    }

    @Test
    fun `signWithResult default wraps a legacy sign result in Success`() = runTest {
        val provider = LegacyProvider { _, _ -> signature }

        val result = provider.signWithResult(payload, documentId)

        val success = result as SignResult.Success
        assertArrayEquals(signature, success.signature)
    }

    @Test
    fun `signWithResult default forwards payload and documentId to legacy sign`() = runTest {
        var seenPayload: ByteArray? = null
        var seenDocumentId: String? = null
        val provider = LegacyProvider { p, d ->
            seenPayload = p
            seenDocumentId = d
            signature
        }

        provider.signWithResult(payload, documentId)

        assertArrayEquals(payload, seenPayload)
        assertTrue(seenDocumentId == documentId)
    }

    @Test
    fun `signWithResult default surfaces a throwing legacy sign by rethrowing`() = runTest {
        val boom = IllegalStateException("legacy signing failed")
        val provider = LegacyProvider { _, _ -> throw boom }

        val thrown = assertFailsWith<IllegalStateException> {
            provider.signWithResult(payload, documentId)
        }
        assertSame(boom, thrown)
    }

    @Test
    fun `signWithResult default wraps a legacy Recoverable into SignResult Failure`() = runTest {
        val recoverable = CredentialSigningException.Recoverable()
        val provider = LegacyProvider { _, _ -> throw recoverable }

        val result = provider.signWithResult(payload, documentId)

        val failure = result as SignResult.Failure
        assertSame(recoverable, failure.exception)
    }

    @Test
    fun `signWithResult default wraps a legacy Unrecoverable into SignResult Failure`() = runTest {
        val unrecoverable = CredentialSigningException.Unrecoverable(RuntimeException("boom"))
        val provider = LegacyProvider { _, _ -> throw unrecoverable }

        val result = provider.signWithResult(payload, documentId)

        val failure = result as SignResult.Failure
        assertSame(unrecoverable, failure.exception)
    }

    @Test
    fun `deprecated sign default unwraps a Success from signWithResult`() = runTest {
        val provider = NewProvider(SignResult.Success(signature))

        @Suppress("DEPRECATION")
        val bytes = provider.sign(payload, documentId)

        assertArrayEquals(signature, bytes)
    }

    @Test
    fun `deprecated sign default throws the exception from a signWithResult Failure`() = runTest {
        val cause = RuntimeException("root cause")
        val provider = NewProvider(
            SignResult.Failure(CredentialSigningException.Unrecoverable(cause))
        )

        val thrown = assertFailsWith<CredentialSigningException.Unrecoverable> {
            @Suppress("DEPRECATION")
            provider.sign(payload, documentId)
        }
        assertSame(cause, thrown.cause)
    }

    @Test
    fun `deprecated sign default propagates a Recoverable failure`() = runTest {
        val provider = NewProvider(
            SignResult.Failure(CredentialSigningException.Recoverable())
        )

        assertFailsWith<CredentialSigningException.Recoverable> {
            @Suppress("DEPRECATION")
            provider.sign(payload, documentId)
        }
    }
}
