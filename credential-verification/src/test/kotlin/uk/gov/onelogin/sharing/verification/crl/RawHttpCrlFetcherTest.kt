package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.android.network.client.v3.RawHttpResponse
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Unavailable

/**
 * Unit tests for [RawHttpCrlFetcher]. Verifies that the fetcher maps [RawHttpClient] responses
 * and transport errors to [CrlLookup] outcomes without touching CRL contents.
 */
class RawHttpCrlFetcherTest {

    private val fakeClient = FakeRawHttpClient()
    private val fetcher = RawHttpCrlFetcher(fakeClient)

    @Test
    fun `HTTP 200 returns Available with the response bytes`() = runBlocking {
        val body = byteArrayOf(1, 2, 3)
        fakeClient.responses[URL] = RawHttpResponse(200, emptyMap(), body)

        val result = fetcher.getCrl(URL)

        result as Available
        assertArrayEquals(body, result.bytes)
    }

    @Test
    fun `HTTP 200 bytes are a caller-owned copy`() = runBlocking {
        val body = byteArrayOf(1, 2, 3)
        fakeClient.responses[URL] = RawHttpResponse(200, emptyMap(), body)

        val first = (fetcher.getCrl(URL) as Available).bytes
        val second = (fetcher.getCrl(URL) as Available).bytes

        // Mutating one result must not affect the other
        first[0] = 99
        assertArrayEquals(byteArrayOf(1, 2, 3), second)
    }

    @Test
    fun `only HTTP 200 is a success - 201 through 5xx are RESPONSE failures`() = runBlocking {
        listOf(201, 204, 206, 301, 400, 404, 500).forEach { code ->
            fakeClient.responses[URL] = RawHttpResponse(code, emptyMap(), ByteArray(0))

            assertEquals("status $code", Unavailable(CrlLookupFailure.RESPONSE), fetcher.getCrl(URL))
        }
    }

    @Test
    fun `IOException becomes NETWORK failure`() = runBlocking {
        fakeClient.failures[URL] = IOException("offline")

        assertEquals(Unavailable(CrlLookupFailure.NETWORK), fetcher.getCrl(URL))
    }

    @Test
    fun `CancellationException propagates and is not swallowed`() {
        fakeClient.failures[URL] = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { fetcher.getCrl(URL) }
        }
    }

    @Test
    fun `forceRevalidation is forwarded to the underlying client`() = runBlocking {
        fakeClient.responses[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))

        fetcher.getCrl(URL)
        fetcher.getCrl(URL, forceRevalidation = true)

        assertEquals(
            listOf(
                FakeRawHttpClient.Request(URL, forced = false),
                FakeRawHttpClient.Request(URL, forced = true),
            ),
            fakeClient.requests,
        )
    }

    private companion object {
        const val URL = "http://dvs.verifier-ca.integration.account.gov.uk/crl/example.crl"
    }
}
