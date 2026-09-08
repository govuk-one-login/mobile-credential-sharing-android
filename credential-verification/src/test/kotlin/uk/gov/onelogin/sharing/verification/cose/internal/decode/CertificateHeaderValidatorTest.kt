package uk.gov.onelogin.sharing.verification.cose.internal.decode

import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.MessageDigest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MalformedCoseSign1
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.MissingX5Chain
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UnsupportedAlgorithm
import uk.gov.onelogin.sharing.verification.cose.internal.decode.InternalCoseSign1.PayloadMode
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject.Companion.MSO_DIGEST_ALGORITHM

@RunWith(TestParameterInjector::class)
class CertificateHeaderValidatorTest {

    private val validator = CertificateHeaderValidator()

    private val leaf = CertificateStubs.leaf
    private val intermediate = CertificateStubs.intermediateCa
    private val root = CertificateStubs.rootCa
    private val other = CertificateStubs.leafSignedByRoot

    /**
     * Every behaviour is exercised for both certificate-backed shapes via [AuthShape]:
     *  - IssuerAuth: attached payload
     *  - ReaderAuth: detached payload
     */
    @Suppress("unused")
    enum class AuthShape(val attached: Boolean) {
        ISSUER_ATTACHED(attached = true),
        READER_DETACHED(attached = false)
    }

    private fun coseSign1(
        protectedHeader: ByteArray,
        unprotectedHeader: ByteArray?,
        shape: AuthShape
    ) = InternalCoseSign1(
        protectedHeader = protectedHeader,
        unprotectedHeader = unprotectedHeader,
        payload = if (shape.attached) byteArrayOf(0x01) else null,
        signature = ByteArray(64),
        payloadMode = if (shape.attached) PayloadMode.ATTACHED else PayloadMode.DETACHED
    )

    @Test
    fun `single-cert x5chain with x5bag in protected header exposes candidate leaf`(
        @TestParameter shape: AuthShape
    ) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf)) {
            put(CoseSign1Builder.X5BAG_LABEL, other.encoded)
        }
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf))

        val profile = validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))

        assertThat(profile.candidateLeaf, equalTo(leaf.encoded))
        assertThat(profile.chain.size, equalTo(1))
        assertThat(profile.chain[0], equalTo(leaf.encoded))
    }

    @Test
    fun `array x5chain with x5bag in unprotected header preserves supplied order`(
        @TestParameter shape: AuthShape
    ) {
        val chain = listOf(leaf, intermediate, root)
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(chain)
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(chain) {
            put(CoseSign1Builder.X5BAG_LABEL, other.encoded)
        }

        val profile = validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))

        assertThat(profile.candidateLeaf, equalTo(leaf.encoded))
        assertThat(profile.chain.size, equalTo(3))
        assertThat(profile.chain[0], equalTo(leaf.encoded))
        assertThat(profile.chain[1], equalTo(intermediate.encoded))
        assertThat(profile.chain[2], equalTo(root.encoded))
    }

    @Test
    fun `no x5chain in either header fails with MissingX5Chain`(@TestParameter shape: AuthShape) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf))
        val unprotectedHeader = CoseSign1Builder.toBytes(CoseSign1Builder.objectNode())

        assertThrows(MissingX5Chain::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `x5bag without x5chain fails with MissingX5Chain`(@TestParameter shape: AuthShape) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf))
        val unprotectedHeader = CoseSign1Builder.objectNode().apply {
            put(CoseSign1Builder.X5BAG_LABEL, other.encoded)
        }.let { CoseSign1Builder.toBytes(it) }

        assertThrows(MissingX5Chain::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `x5chain in protected header fails with MalformedCoseSign1`(
        @TestParameter shape: AuthShape
    ) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf)) {
            CoseSign1Builder.putX5Chain(this, listOf(leaf))
        }
        val unprotectedHeader = CoseSign1Builder.toBytes(CoseSign1Builder.objectNode())

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `protected x5chain rejected even when unprotected x5chain present`(
        @TestParameter shape: AuthShape
    ) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf)) {
            CoseSign1Builder.putX5Chain(this, listOf(leaf))
        }
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf))

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `empty x5chain array fails with MalformedCoseSign1`(@TestParameter shape: AuthShape) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf))
        val unprotectedHeader = CoseSign1Builder.objectNode().apply {
            set<ArrayNode>(CoseSign1Builder.X5CHAIN_LABEL, arrayNode())
        }.let { CoseSign1Builder.toBytes(it) }

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `invalid x5chain fails with MalformedCoseSign1`(
        @TestParameter shape: AuthShape,
        @TestParameter(valuesProvider = InvalidX5ChainProvider::class) case: InvalidX5ChainCase
    ) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(listOf(leaf))
        val unprotectedHeader = CoseSign1Builder.objectNode()
            .apply(case.applyTo)
            .let { CoseSign1Builder.toBytes(it) }

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `protected x5t missing fails with MalformedCoseSign1`(
        @TestParameter shape: AuthShape,
        @TestParameter(valuesProvider = MissingProtectedX5tProvider::class) case: UnprotectedX5tCase
    ) {
        val protectedHeader = CoseSign1Builder.protectedHeaderBytes(
            listOf(leaf),
            includeX5t = false
        )
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf), case.applyTo)

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `malformed protected x5t fails with MalformedCoseSign1`(
        @TestParameter shape: AuthShape,
        @TestParameter(valuesProvider = MalformedX5tProvider::class) case: MalformedX5tCase
    ) {
        val protectedHeader =
            CoseSign1Builder.protectedHeaderBytes(listOf(leaf), includeX5t = false) {
                case.applyTo(this)
            }
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf))

        assertThrows(MalformedCoseSign1::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    @Test
    fun `non-SHA-256 x5t algorithm fails with UnsupportedAlgorithm`(
        @TestParameter shape: AuthShape
    ) {
        // -43 = SHA-384 in the COSE algorithm registry, with a byte-string hash value.
        val protectedHeader =
            CoseSign1Builder.protectedHeaderBytes(listOf(leaf), includeX5t = false) {
                set<ArrayNode>(
                    CoseSign1Builder.X5T_LABEL,
                    CoseSign1Builder.x5tNode(-43, ByteArray(48))
                )
            }
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf))

        assertThrows(UnsupportedAlgorithm::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }

    // --- AC8: mismatched thumbprint ----------------------------------------

    @Test
    fun `AC8 32-byte SHA-256 x5t that does not match the leaf fails with InvalidSignature`(
        @TestParameter shape: AuthShape
    ) {
        val wrongHash = MessageDigest.getInstance(MSO_DIGEST_ALGORITHM).digest(other.encoded)
        val protectedHeader =
            CoseSign1Builder.protectedHeaderBytes(listOf(leaf), includeX5t = false) {
                set<ArrayNode>(
                    CoseSign1Builder.X5T_LABEL,
                    CoseSign1Builder.x5tNode(CoseSign1Builder.COSE_ALG_SHA_256, wrongHash)
                )
            }
        val unprotectedHeader = CoseSign1Builder.unprotectedHeaderBytes(listOf(leaf))

        assertThrows(InvalidSignature::class.java) {
            validator.validate(coseSign1(protectedHeader, unprotectedHeader, shape))
        }
    }
}
