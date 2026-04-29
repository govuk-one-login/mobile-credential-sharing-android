package uk.gov.onelogin.sharing.orchestration.holder.credential

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.FakeRawCredentialParser
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParsingException
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl.Companion.LOG_DOCTYPE_MISMATCH
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl.Companion.LOG_GET_CREDENTIALS_ERROR
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl.Companion.LOG_MSO_DECODE_ERROR
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandlerImpl.Companion.LOG_NO_CREDENTIALS

class CredentialRequestHandlerImplTest {
    private val docType = "org.iso.18013.5.1.mDL"
    private val nameSpaces = byteArrayOf(0xA0.toByte())
    private val issuerAuth = byteArrayOf(0x01)

    private val fakeCredentialProvider = FakeCredentialProvider().apply {
        credentialsToReturn = listOf(
            Credential(id = "test-id", rawCredential = byteArrayOf(0x01))
        )
    }

    private val fakeParser = FakeRawCredentialParser().apply {
        resultToReturn = ParsedRawCredential(
            nameSpaces = nameSpaces,
            issuerAuth = issuerAuth,
            msoDocType = docType
        )
    }

    private val handler = CredentialRequestHandlerImpl(
        credentialProvider = fakeCredentialProvider,
        rawCredentialParser = fakeParser
    )

    @Test
    fun `requestAndValidate returns ValidatedCredential on success`() = runTest {
        val result = handler.requestAndValidate(docType)

        assertEquals("test-id", result.credentialId)
        assertArrayEquals(nameSpaces, result.nameSpaces)
        assertArrayEquals(issuerAuth, result.issuerAuth)
        assertEquals(
            listOf(docType),
            fakeCredentialProvider.lastRequest?.documentTypes
        )
    }

    @Test
    fun `requestAndValidate throws when provider throws`() = runTest {
        fakeCredentialProvider.getCredentialsException = RuntimeException("host app error")

        val exception = try {
            handler.requestAndValidate(docType)
            null
        } catch (e: CredentialRequestException) {
            e
        }

        assertEquals(LOG_GET_CREDENTIALS_ERROR, exception?.message)
    }

    @Test
    fun `requestAndValidate throws when provider returns empty list`() = runTest {
        fakeCredentialProvider.credentialsToReturn = emptyList()

        val exception = try {
            handler.requestAndValidate(docType)
            null
        } catch (e: CredentialRequestException) {
            e
        }

        assertEquals(LOG_NO_CREDENTIALS, exception?.message)
    }

    @Test
    fun `requestAndValidate throws when parser throws`() = runTest {
        fakeParser.exceptionToThrow = RawCredentialParsingException("invalid cbor")

        val exception = try {
            handler.requestAndValidate(docType)
            null
        } catch (e: CredentialRequestException) {
            e
        }

        assertEquals(LOG_MSO_DECODE_ERROR, exception?.message)
    }

    @Test
    fun `requestAndValidate throws when msoDocType does not match requested docType`() = runTest {
        fakeParser.resultToReturn = ParsedRawCredential(
            nameSpaces = nameSpaces,
            issuerAuth = issuerAuth,
            msoDocType = "invalid_doc_type"
        )

        val exception = try {
            handler.requestAndValidate(docType)
            null
        } catch (e: CredentialRequestException) {
            e
        }

        assertEquals(LOG_DOCTYPE_MISMATCH, exception?.message)
    }

    @Test
    fun `requestAndValidate selects first credential when multiple returned`() = runTest {
        fakeCredentialProvider.credentialsToReturn = listOf(
            Credential(id = "first", rawCredential = byteArrayOf(0x01)),
            Credential(id = "second", rawCredential = byteArrayOf(0x02))
        )

        val result = handler.requestAndValidate(docType)
        assertEquals("first", result.credentialId)
    }
}
