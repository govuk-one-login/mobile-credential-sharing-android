package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.android.network.client.v3.RawHttpResponse

class CrlPrefetchDownloaderTest {
    private val fake = FakeRawHttpClient()

    private fun downloader(vararg urls: String) =
        CrlPrefetchDownloader(urls.toList(), CrlEndpointClient(fake))

    @Test
    fun `requests each configured url once in order`() = runBlocking {
        fake.response = RawHttpResponse(200, emptyMap(), byteArrayOf(1))

        val results = downloader(A, B).downloadAll()

        assertEquals(listOf(A, B), fake.requestedUrls)
        assertEquals(listOf(A, B), results.keys.toList())
    }

    @Test
    fun `empty configuration makes no requests`() = runBlocking {
        val results = downloader().downloadAll()

        assertTrue(results.isEmpty())
        assertTrue(fake.requestedUrls.isEmpty())
    }

    @Test
    fun `one failing url does not stop the others`() = runBlocking {
        val offline = IOException("offline")
        fake.failuresByUrl[A] = offline
        fake.responsesByUrl[B] = RawHttpResponse(404, emptyMap(), ByteArray(0))

        val results = downloader(A, B).downloadAll()

        assertEquals(CrlFetchResult.Unreachable(offline), results[A])
        assertEquals(CrlFetchResult.HttpError(404), results[B])
    }

    @Test
    fun `cancellation propagates`() {
        fake.failure = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { downloader(A, B).downloadAll() }
        }
        assertEquals(listOf(A), fake.requestedUrls)
    }

    private companion object {
        const val A = "http://dvs.verifier-ca.integration.account.gov.uk/crl/a.crl"
        const val B = "http://root.verifier-root-ca.integration.account.gov.uk/crl/b.crl"
    }
}
