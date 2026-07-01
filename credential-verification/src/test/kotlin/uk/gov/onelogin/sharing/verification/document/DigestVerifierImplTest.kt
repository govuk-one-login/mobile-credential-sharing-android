package uk.gov.onelogin.sharing.verification.document

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.MessageDigest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingVerifiableDocument
import uk.gov.onelogin.sharing.verification.document.IssuerSignedItemStubs.issuerSignedItemBytes
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError

@RunWith(TestParameterInjector::class)
class DigestVerifierImplTest {

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

    data class DigestCase(
        val itemBytes: ByteArray,
        val msoDigests: Map<String, Map<Int, ByteArray>>
    )

    @Test
    fun `verify throws DIGEST_MISMATCH`(
        @TestParameter case: DigestCase = namedTestValuesIn(digestMismatchCases)
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(case.itemBytes)),
                MobileSecurityObjectStub.create(valueDigests = case.msoDigests)
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISMATCH))
    }

    @Test
    fun `verify throws DIGEST_MISSING`(
        @TestParameter case: DigestCase = namedTestValuesIn(digestMissingCases)
    ) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verify(
                documentWithItems(NAMESPACE to listOf(case.itemBytes)),
                MobileSecurityObjectStub.create(valueDigests = case.msoDigests)
            )
        }
        assertThat(exception, hasError(VerificationError.DIGEST_MISSING))
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

    companion object {
        private const val NAMESPACE = MobileSecurityObject.NAMESPACE

        private val digestMismatchCases = mapOf(
            "HASH_DOES_NOT_MATCH" to DigestCase(
                itemBytes = issuerSignedItemBytes("given_name", "Alice"),
                msoDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32) { 0xFF.toByte() }))
            ),
            "INVALID_TAG24_ENCODING" to DigestCase(
                itemBytes = CborMapper.default.writeValueAsBytes("not a binary node"),
                msoDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
            ),
            "NEGATIVE_DIGEST_ID" to DigestCase(
                itemBytes = issuerSignedItemBytes("family_name", "X", digestId = -1),
                msoDigests = mapOf(NAMESPACE to mapOf(-1 to ByteArray(32)))
            ),
            "DIGEST_ID_AT_MAX_BOUNDARY" to DigestCase(
                itemBytes = issuerSignedItemBytes("x", "y", digestId = 2_147_483_648L),
                msoDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
            )
        )

        private val digestMissingCases = mapOf(
            "ID_NOT_FOUND" to DigestCase(
                itemBytes = issuerSignedItemBytes("age_over_18", "true", digestId = 5),
                msoDigests = mapOf(NAMESPACE to mapOf(0 to ByteArray(32)))
            ),
            "NAMESPACE_NOT_IN_MSO" to DigestCase(
                itemBytes = issuerSignedItemBytes("family_name", "Smith"),
                msoDigests = emptyMap()
            )
        )
    }
}
