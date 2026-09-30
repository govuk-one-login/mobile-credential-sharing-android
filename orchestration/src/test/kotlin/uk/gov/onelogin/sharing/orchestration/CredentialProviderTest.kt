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

        @Deprecated("Superseded by signV2, which returns a SignResult.")
        override suspend fun sign(payload: ByteArray, documentId: String): ByteArray =
            onSign(payload, documentId)
    }

    /** New provider: overrides only [signV2], returning a [SignResult]. */
    private class NewProvider(private val result: SignResult) : CredentialProvider {
        override suspend fun getCredentials(request: CredentialRequest): List<Credential> =
            emptyList()

        override suspend fun signV2(payload: ByteArray, documentId: String): SignResult = result
    }

    @Test
    fun `signV2 default wraps a legacy sign result in Success`() = runTest {
        val provider = LegacyProvider { _, _ -> signature }

        val result = provider.signV2(payload, documentId)

        val success = result as SignResult.Success
        assertArrayEquals(signature, success.signature)
    }

    @Test
    fun `signV2 default forwards payload and documentId to legacy sign`() = runTest {
        var seenPayload: ByteArray? = null
        var seenDocumentId: String? = null
        val provider = LegacyProvider { p, d ->
            seenPayload = p
            seenDocumentId = d
            signature
        }

        provider.signV2(payload, documentId)

        assertArrayEquals(payload, seenPayload)
        assertTrue(seenDocumentId == documentId)
    }

    @Test
    fun `signV2 default surfaces a throwing legacy sign by rethrowing`() = runTest {
        val boom = IllegalStateException("legacy signing failed")
        val provider = LegacyProvider { _, _ -> throw boom }

        val thrown = assertFailsWith<IllegalStateException> {
            provider.signV2(payload, documentId)
        }
        assertSame(boom, thrown)
    }

    @Test
    fun `deprecated sign default unwraps a Success from signV2`() = runTest {
        val provider = NewProvider(SignResult.Success(signature))

        @Suppress("DEPRECATION")
        val bytes = provider.sign(payload, documentId)

        assertArrayEquals(signature, bytes)
    }

    @Test
    fun `deprecated sign default throws the exception from a signV2 Failure`() = runTest {
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
