package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v2CRLBuilder
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.android.network.client.v3.RawHttpResponse

class CrlEndpointClientTest {
    private val fake = FakeRawHttpClient()
    private val client = CrlEndpointClient(fake)

    @Test
    fun `requests the given url and passes forceRevalidation through`() = runBlocking {
        fake.response = response(200, byteArrayOf(1))

        client.fetch(URL, forceRevalidation = true)

        assertEquals(listOf(URL), fake.requestedUrls)
        assertEquals(true, fake.requestedForce)
    }

    @Test
    fun `parses a DER crl and surfaces its cache control header`() = runBlocking {
        fake.response = response(200, derCrl(), "Cache-Control" to listOf("max-age=3600"))

        val result = client.fetch(URL)

        assertTrue(result is CrlFetchResult.Success)
        result as CrlFetchResult.Success
        assertEquals("CN=Test CRL Issuer", result.crl.issuerX500Principal.name)
        assertEquals("max-age=3600", result.cacheControl)
    }

    @Test
    fun `non-crl body is reported as malformed`() = runBlocking {
        fake.response = response(200, "not a crl".toByteArray())

        assertTrue(client.fetch(URL) is CrlFetchResult.Malformed)
    }

    @Test
    fun `non-success status is reported as http error`() = runBlocking {
        fake.response = response(404, ByteArray(0))

        assertEquals(CrlFetchResult.HttpError(404), client.fetch(URL))
    }

    @Test
    fun `connection failure is reported as unreachable`() = runBlocking {
        val failure = IOException("offline")
        fake.failure = failure

        assertEquals(CrlFetchResult.Unreachable(failure), client.fetch(URL))
    }

    private fun response(status: Int, body: ByteArray, vararg headers: Pair<String, List<String>>) =
        RawHttpResponse(status = status, headers = mapOf(*headers), body = body)

    private fun derCrl(): ByteArray {
        val keys = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp384r1")) }
            .generateKeyPair()
        val now = System.currentTimeMillis()
        val holder = X509v2CRLBuilder(X500Name("CN=Test CRL Issuer"), Date(now))
            .setNextUpdate(Date(now + WEEK_MILLIS))
            .build(JcaContentSignerBuilder("SHA384withECDSA").build(keys.private))
        return JcaX509CRLConverter().getCRL(holder).encoded
    }

    private companion object {
        const val URL = "http://dvs.verifier-ca.integration.account.gov.uk/crl/" +
            "85b2ae92-97ab-406b-93b2-f765795de390.crl"
        const val WEEK_MILLIS = 7L * 24 * 60 * 60 * 1000
    }
}
