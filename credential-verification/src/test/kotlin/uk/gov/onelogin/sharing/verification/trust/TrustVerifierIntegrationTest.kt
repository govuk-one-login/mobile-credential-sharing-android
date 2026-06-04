package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TrustVerifierIntegrationTest {
    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `CoseSign1Decoder decodes issuerAuth from mock credential`() {
        val issuerAuth = extractIssuerAuth()

        val coseSign1 = CoseSign1Decoder.decode(issuerAuth)

        assertNotNull(coseSign1.protectedHeader)
        assertNotNull(coseSign1.unprotectedHeader)
        assertNotNull(coseSign1.payload)
        assertNotNull(coseSign1.signature)
    }

    @Test
    fun `CoseSign1Decoder extracts x5chain from mock credential`() {
        val issuerAuth = extractIssuerAuth()
        val coseSign1 = CoseSign1Decoder.decode(issuerAuth)
        val x5chain = CoseSign1Decoder.extractX5Chain(coseSign1)

        assertEquals(x5chain.size, 1)
    }

    @Test
    fun `x5chain certificate from mock credential is valid X509`() {
        val issuerAuth = extractIssuerAuth()
        val cert = extractCertFromIssuerAuth(issuerAuth)

        assertEquals(cert.subjectX500Principal.name.contains("mDoc Test Issuer"), true)
    }

    @Test
    fun `verifyCOSESign1 returns correct subjectCountry from mock credential`() {
        val issuerAuth = extractIssuerAuth()
        val trustedRoot = extractCertFromIssuerAuth(issuerAuth)
        val verifier = TrustVerifierImpl()

        val result = verifier.verifyCOSESign1(issuerAuth, trustedRoot)

        assertEquals("GB", result.subjectCountry)
        assertEquals("London", result.subjectState)
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
}
