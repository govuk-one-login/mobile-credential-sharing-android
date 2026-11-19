package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.DAY
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.NOW
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.ok
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.status
import uk.gov.onelogin.sharing.verification.crl.CrlRefreshResult.READY
import uk.gov.onelogin.sharing.verification.crl.CrlRefreshResult.UPDATE_REQUIRED

class CrlProviderRefreshTest {
    private val fake = FakeRawHttpClient()

    private fun provider(vararg urls: String) =
        CrlProvider(urls.toList(), fake, now = { NOW })

    private fun valid(url: String) {
        fake.responses[url] = ok(CrlFixtures.validCrl())
    }

    @Test
    fun `empty configuration is ready without any request`() = runBlocking {
        assertEquals(READY, provider().refresh())
        assertTrue(fake.requests.isEmpty())
    }

    @Test
    fun `every configured crl valid is ready and each url is requested once`() = runBlocking {
        valid(A)
        valid(B)

        assertEquals(READY, provider(A, B).refresh())
        assertEquals(listOf(A, B), fake.requestedUrls)
    }

    @Test
    fun `an unavailable crl needs an update but the others are still requested`() = runBlocking {
        fake.failures[A] = IOException("offline")
        valid(B)

        assertEquals(UPDATE_REQUIRED, provider(A, B).refresh())
        assertEquals(listOf(A, B), fake.requestedUrls)
    }

    @Test
    fun `a non 200 response needs an update`() = runBlocking {
        fake.responses[A] = status(404)

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
    }

    @Test
    fun `an expired crl is revalidated once and ready if the server has a newer one`() = runBlocking {
        fake.responses[A] = ok(CrlFixtures.expiredCrl())
        fake.forcedResponses[A] = ok(CrlFixtures.validCrl())

        assertEquals(READY, provider(A).refresh())
        assertEquals(
            listOf(FakeRawHttpClient.Request(A, false), FakeRawHttpClient.Request(A, true)),
            fake.requests
        )
    }

    @Test
    fun `a crl that stays expired needs an update after exactly one forced check`() = runBlocking {
        fake.responses[A] = ok(CrlFixtures.expiredCrl())

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
        assertEquals(1, fake.requests.count { it.forced })
    }

    @Test
    fun `a crl whose next update is exactly now is expired`() = runBlocking {
        fake.responses[A] = ok(CrlFixtures.crl(thisUpdate = NOW - DAY, nextUpdate = NOW))

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
    }

    @Test
    fun `a crl issued in the future needs an update without a forced check`() = runBlocking {
        fake.responses[A] = ok(CrlFixtures.crl(thisUpdate = NOW + DAY, nextUpdate = NOW + 7 * DAY))

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
        assertEquals(1, fake.requests.size)
    }

    @Test
    fun `an unreadable body needs an update`() = runBlocking {
        fake.responses[A] = ok("not a crl".toByteArray())

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
    }

    @Test
    fun `a crl without next update needs an update`() = runBlocking {
        fake.responses[A] = ok(CrlFixtures.crl(thisUpdate = NOW - DAY, nextUpdate = null))

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
    }

    @Test
    fun `a response the cache did not retain cannot make the device ready`() = runBlocking {
        valid(A)
        fake.retainResponses = false

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
    }

    @Test
    fun `discovered urls do not affect readiness and are not refreshed`() = runBlocking {
        valid(A)
        fake.failures[DISCOVERED] = IOException("offline")
        val provider = provider(A)

        provider.getCrl(DISCOVERED)

        assertEquals(READY, provider.refresh())
        assertEquals(listOf(DISCOVERED, A), fake.requestedUrls)
    }

    @Test
    fun `cancellation propagates`() {
        fake.failures[A] = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { provider(A).refresh() }
        }
    }

    private companion object {
        const val A = "http://dvs.verifier-ca.integration.account.gov.uk/crl/a.crl"
        const val B = "http://root.verifier-root-ca.integration.account.gov.uk/crl/b.crl"
        const val DISCOVERED = "http://dvs.l4.integration.account.gov.uk/crl/l4.crl"
    }
}
