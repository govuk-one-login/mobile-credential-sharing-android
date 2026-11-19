package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.ok
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.status

class CrlProviderGetCrlTest {
    private val fake = FakeRawHttpClient()
    private val provider = CrlProvider(listOf(CONFIGURED), fake, now = { CrlFixtures.NOW })

    @Test
    fun `http 200 returns the bytes`() = runBlocking {
        val bytes = byteArrayOf(1, 2, 3)
        fake.responses[CONFIGURED] = ok(bytes)

        assertEquals(CrlLookup.Available(bytes), provider.getCrl(CONFIGURED))
    }

    @Test
    fun `any other status is a response failure`() = runBlocking {
        listOf(204, 206, 301, 404, 500).forEach { code ->
            fake.responses[CONFIGURED] = status(code)

            assertEquals(
                "status $code",
                CrlLookup.Unavailable(CrlLookupFailure.RESPONSE),
                provider.getCrl(CONFIGURED)
            )
        }
    }

    @Test
    fun `io failure is a network failure`() = runBlocking {
        fake.failures[CONFIGURED] = IOException("offline")

        assertEquals(CrlLookup.Unavailable(CrlLookupFailure.NETWORK), provider.getCrl(CONFIGURED))
    }

    @Test
    fun `cancellation propagates instead of becoming unavailable`() {
        fake.failures[CONFIGURED] = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { provider.getCrl(CONFIGURED) }
        }
    }

    @Test
    fun `force revalidation is passed to the client and defaults to false`() = runBlocking {
        fake.responses[CONFIGURED] = ok(byteArrayOf(1))

        provider.getCrl(CONFIGURED)
        provider.getCrl(CONFIGURED, forceRevalidation = true)

        assertEquals(listOf(false, true), fake.requests.map { it.forced })
    }

    @Test
    fun `a url that is not configured is retrieved on demand`() = runBlocking {
        fake.responses[DISCOVERED] = ok(byteArrayOf(9))

        assertEquals(CrlLookup.Available(byteArrayOf(9)), provider.getCrl(DISCOVERED))
        assertEquals(listOf(DISCOVERED), fake.requestedUrls)
    }

    private companion object {
        const val CONFIGURED = "http://dvs.verifier-ca.integration.account.gov.uk/crl/a.crl"
        const val DISCOVERED = "http://dvs.l4.integration.account.gov.uk/crl/l4.crl"
    }
}
