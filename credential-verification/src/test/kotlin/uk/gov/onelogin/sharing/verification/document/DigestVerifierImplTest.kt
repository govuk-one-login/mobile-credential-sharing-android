package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.security.MessageDigest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocument
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemStubs.issuerSignedItemBytes
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

class DigestVerifierImplTest {

    private val cborMapper = ObjectMapper(CBORFactory())
    private val verifier = DigestVerifierImpl()

    @Test
    fun `verify does not throw when digests match`() {
        val itemBytes = issuerSignedItemBytes("family_name", "Smith")
        verifier.verify(
            documentWithItems(NAMESPACE to listOf(itemBytes)),
            MobileSecurityObjectStub.create(
                valueDigests = mapOf(NAMESPACE to mapOf(0 to sha256(itemBytes)))
            )
        )
    }

    @Test
    fun `verify does not throw when nameSpaces is null`() {
        val document = SharingVerifiableDocument(
            docType = MobileSecurityObject.DOC_TYPE,
            issuerSigned = SharingIssuerSigned(issuerAuth = byteArrayOf(), nameSpaces = null)
        )
        verifier.verify(document, MobileSecurityObjectStub.create())
    }

    @Test
    fun `verify throws DIGEST_MISMATCH when hash does not match`() {
        val itemBytes = issuerSignedItemBytes("given_name", "Alice")

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(itemBytes)),
                MobileSecurityObjectStub.create(
                    valueDigests = mapOf(
                        NAMESPACE to mapOf(
                            0 to ByteArray(32) {
                                0xFF.toByte()
                            }
                        )
                    )
                )
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISMATCH))
    }

    @Test
    fun `verify throws DIGEST_MISSING when digestId not found in MSO`() {
        val itemBytes = issuerSignedItemBytes("age_over_18", "true", digestId = 5)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(itemBytes)),
                MobileSecurityObjectStub.create(
                    valueDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
                )
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISSING))
    }

    @Test
    fun `verify throws DIGEST_MISSING when namespace not in MSO`() {
        val itemBytes = issuerSignedItemBytes("family_name", "Smith")

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(itemBytes)),
                MobileSecurityObjectStub.create()
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISSING))
    }

    @Test
    fun `verify throws DIGEST_MISMATCH for invalid Tag 24 encoding`() {
        val invalidBytes = cborMapper.writeValueAsBytes("not a binary node")

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(invalidBytes)),
                MobileSecurityObjectStub.create(
                    valueDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
                )
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISMATCH))
    }

    @Test
    fun `verify throws DIGEST_MISMATCH for negative digestId`() {
        val itemBytes = issuerSignedItemBytes("family_name", "X", digestId = -1)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(itemBytes)),
                MobileSecurityObjectStub.create(
                    valueDigests = mapOf(NAMESPACE to mapOf(-1 to ByteArray(32)))
                )
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISMATCH))
    }

    @Test
    fun `verify throws DIGEST_MISMATCH for digestId at max boundary`() {
        val itemBytes = issuerSignedItemBytes("x", "y", digestId = 2_147_483_648L)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(itemBytes)),
                MobileSecurityObjectStub.create(
                    valueDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
                )
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISMATCH))
    }

    @Test
    fun `verify succeeds with multiple namespaces and items`() {
        val item1 = issuerSignedItemBytes("family_name", "Smith", digestId = 0)
        val item2 = issuerSignedItemBytes("given_name", "John", digestId = 1)
        val item3 = issuerSignedItemBytes("vehicle_class", "B", digestId = 0)
        val ns2 = "org.iso.18013.5.1.aamva"

        verifier.verify(
            documentWithItems(NAMESPACE to listOf(item1, item2), ns2 to listOf(item3)),
            MobileSecurityObjectStub.create(
                valueDigests = mapOf(
                    NAMESPACE to mapOf(0 to sha256(item1), 1 to sha256(item2)),
                    ns2 to mapOf(0 to sha256(item3))
                )
            )
        )
    }

    private fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    private fun documentWithItems(
        vararg nameSpaces: Pair<String, List<ByteArray>>
    ): VerifiableDocument = SharingVerifiableDocument(
        docType = MobileSecurityObject.DOC_TYPE,
        issuerSigned = SharingIssuerSigned(
            issuerAuth = byteArrayOf(),
            nameSpaces = nameSpaces.toMap()
        )
    )

    private companion object {
        const val NAMESPACE = MobileSecurityObject.NAMESPACE
    }
}
