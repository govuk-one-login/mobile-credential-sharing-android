package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.cose.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

class TrustVerifierImplTest {
    private val logger: Logger = SystemLogger()
    private val decoder = CoseSign1Decoder(logger)
    private val verifier = TrustVerifierImpl(
        decoder,
        CoseSignatureVerifier(CoseHeaderValidator(logger))
    )
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `verifyCOSESign1 with empty bytes throws MALFORMED_ISSUER_AUTH`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk())
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `verifyCOSESign1 with detached payload throws INVALID_DEVICE_SIGNATURE`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(byteArrayOf(), mockk(), byteArrayOf())
        }
        assertThat(exception, hasError(VerificationError.INVALID_DEVICE_SIGNATURE))
    }

    @Test
    fun `CoseSign1Decoder decodes issuerAuth from mock credential`() {
        val issuerAuth = extractIssuerAuth()
        val coseSign1 = decoder.decode(issuerAuth)

        assertNotNull(coseSign1.protectedHeader)
        assertNotNull(coseSign1.unprotectedHeader)
        assertNotNull(coseSign1.payload)
        assertNotNull(coseSign1.signature)
    }

    @Test
    fun `CoseSign1Decoder extracts x5chain from mock credential`() {
        val issuerAuth = extractIssuerAuth()
        val coseSign1 = decoder.decode(issuerAuth)
        val x5chain = decoder.extractX5Chain(coseSign1)

        assertEquals(x5chain.size, 1)
    }

    @Test
    fun `valid IssuerAuth returns leaf validity period, MSO payload, and subject attributes`() {
        val issuerAuth = extractIssuerAuth()
        val cert = extractCertFromIssuerAuth(issuerAuth)
        val result = verifier.verifyCOSESign1(issuerAuth, cert)

        assertNotNull(result.certificateValidityPeriod)
        assertNotNull(result.msoPayload)
        assertEquals(result.subjectCountry, "GB")
        assertEquals(result.subjectState, "London")
    }

    @Test
    fun `non-COSE_Sign1 bytes throw MALFORMED_ISSUER_AUTH`() {
        val invalidCbor = byteArrayOf(0xA1.toByte(), 0x01, 0x02)
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(invalidCbor, mockk())
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    @Test
    fun `tampered signature throws INVALID_ISSUER_SIGNATURE`() {
        val issuerAuth = extractIssuerAuth()
        val cert = extractCertFromIssuerAuth(issuerAuth)

        val root = cborMapper.readTree(issuerAuth) as ArrayNode
        val sigBytes = (root[3] as BinaryNode).binaryValue()
        sigBytes[0] = (sigBytes[0].toInt() xor 0xFF).toByte()

        val tampered = cborMapper.createArrayNode()
        tampered.add(root[0])
        tampered.add(root[1])
        tampered.add(root[2])
        tampered.add(sigBytes)
        val tamperedBytes = cborMapper.writeValueAsBytes(tampered)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(tamperedBytes, cert)
        }
        assertThat(exception, hasError(VerificationError.INVALID_ISSUER_SIGNATURE))
    }

    @Test
    fun `unanchored chain throws UNTRUSTED_CERTIFICATE`() {
        val issuerAuth = extractIssuerAuth()

        val keyPairGen = KeyPairGenerator.getInstance("EC")
        keyPairGen.initialize(ECGenParameterSpec("secp256r1"))
        val untrustedKeyPair = keyPairGen.generateKeyPair()

        val untrustedRoot = mockk<X509Certificate>()
        every { untrustedRoot.publicKey } returns untrustedKeyPair.public

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(issuerAuth, untrustedRoot)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `non-EC leaf public key throws MALFORMED_ISSUER_AUTH`() {
        val rsaCertDer = buildRsaCertDer()
        val rsaCert = CertificateFactory.getInstance("X.509")
            .generateCertificate(ByteArrayInputStream(rsaCertDer)) as X509Certificate

        val coseWithRsaCert = buildCoseSign1WithCertBytes(rsaCertDer)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseWithRsaCert, rsaCert)
        }
        assertThat(exception, hasError(VerificationError.MALFORMED_ISSUER_AUTH))
    }

    private fun extractIssuerAuth(): ByteArray {
        val credentialBase64 = javaClass.classLoader!!
            .getResourceAsStream("mock_credential.txt")!!
            .bufferedReader()
            .readText()
            .trim()

        val padded = credentialBase64
            .replace('-', '+')
            .replace('_', '/')
            .let { it + "=".repeat((4 - it.length % 4) % 4) }

        val credentialBytes = Base64.getDecoder().decode(padded)
        val root = cborMapper.readTree(credentialBytes)
        val issuerAuthNode = root.get("issuerAuth") as ArrayNode
        return cborMapper.writeValueAsBytes(issuerAuthNode)
    }

    private fun extractCertFromIssuerAuth(issuerAuth: ByteArray): X509Certificate {
        val root = cborMapper.readTree(issuerAuth) as ArrayNode
        val unprotected = root[1]
        val certBytes = (unprotected.get("33") as BinaryNode).binaryValue()
        val certFactory = CertificateFactory.getInstance("X.509")
        return certFactory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
    }

    private fun buildRsaCertDer(): ByteArray {
        // Self-signed RSA certificate (CN=RSA Test, C=GB, ST=London)
        return ("30820306308201eea003020102020900e69e74670ddd453f300d06092a864886" +
            "f70d01010b05003031310f300d060355040813064c6f6e646f6e310b30090603" +
            "550406130247423111300f060355040313085253412054657374301e170d3236" +
            "303631303130343230365a170d3336303630373130343230365a3031310f300d" +
            "060355040813064c6f6e646f6e310b30090603550406130247423111300f0603" +
            "5504031308525341205465737430820122300d06092a864886f70d0101010500" +
            "0382010f003082010a0282010100b38ac99cbe3435d3e2b83e0dc01ad4508d0c" +
            "52fffb38fd78da98ccccb2df34d29fa4343f7cff9b895ef635cf1177f61d3cc1" +
            "88c4e4e6e56d0b3385f045ad4bf3c43052457866e7e4426d80fcae0750b82dca" +
            "9bcafc4e8eff6822396891afa8ac8ebde0c581d2be9dd1f85e33b2066d806dc3" +
            "4451e4cb9038f6b31e4644aa6b0eb6b9fcbf708aead4c4e91aa4347e4ab4d45b" +
            "a4fcd625f80dc1919a4c1fb0057157d0e6a90aada0ffb071e01a76c5e001a116" +
            "8325adcb0a145fe39b193cba8a5dd88dc8e2f1e0ae01a2fa09d98a9a2612e758" +
            "578a83a5cea869bf0c8aa54a7067b9fd95aa771a4ec40efc655a925c6bbb1e6e" +
            "fa1c04fa9278a5f52d55c10cd5090203010001a321301f301d0603551d0e0416" +
            "04141a6163043a2b5643af8f6eab86461fb50e937812300d06092a864886f70d" +
            "01010b05000382010100a0452bf71674458cf32cf85784732e38f605b78348f6" +
            "c488a66c2412e47cee5089ef31877381c9a695f406e83c6b7e00728e23f501e0" +
            "8405860b267fa1f92721682eba56c0075151db4bee8e119bcb7fad2159dc7906" +
            "f758819cb71308a68393266a36b4caeb47cbf16495e32f5537c34930190945743" +
            "145d2a06efa486fbaa128c9b320b0bdc9a413abc7f5e408a76a2ec281c069d2f" +
            "376f49d8d914434310a2529459e5f4dec414875b4bc820a0091d273e78ced205" +
            "654d509085cee1bfabc1eff427e7ee1ed201731b45bc2228ae19b753bc81c210" +
            "f164e248f41da2b4b5e29761932d0ddc90bb82d9341a576e0ec275dffe60f716" +
            "7fed067bd175f651a63").hexToByteArray()
    }

    private fun buildCoseSign1WithCertBytes(certDer: ByteArray): ByteArray {
        val protectedHeader = cborMapper.createObjectNode()
        protectedHeader.put("1", -7)
        val protectedBytes = cborMapper.writeValueAsBytes(protectedHeader)

        val unprotectedHeader = cborMapper.createObjectNode()
        unprotectedHeader.put("33", certDer)
        val unprotectedBytes = cborMapper.writeValueAsBytes(unprotectedHeader)

        val payload = byteArrayOf(0x01, 0x02, 0x03)
        val signature = ByteArray(64)

        val array = cborMapper.createArrayNode()
        array.add(protectedBytes)
        array.add(cborMapper.readTree(unprotectedBytes))
        array.add(payload)
        array.add(signature)

        return cborMapper.writeValueAsBytes(array)
    }
}
