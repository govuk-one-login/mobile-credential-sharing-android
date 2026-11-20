package uk.gov.onelogin.sharing.verification.crl

import java.util.concurrent.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.EXPIRED
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.UNAVAILABLE
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.USABLE
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Unavailable
import uk.gov.onelogin.sharing.verification.crl.CrlRefreshResult.READY
import uk.gov.onelogin.sharing.verification.crl.CrlRefreshResult.UPDATE_REQUIRED

/**
 * Unit tests for [CrlProvider] coordinating [CrlFetcher] and [CrlCacheInspector].
 *
 * Both collaborators are faked here so each test is fast, deterministic and focused on the
 * coordination logic only. Fetcher and inspector behaviours are verified in their own tests.
 */
class CrlProviderTest {

    private val fakeFetcher = FakeCrlFetcher()
    private val fakeInspector = FakeCrlCacheInspector()

    /** Constructs a provider using the internal constructor that accepts collaborator fakes. */
    private fun provider(vararg urls: String) =
        CrlProvider(urls.toList(), fakeFetcher, fakeInspector)

    @Test
    fun `getCrl delegates to the fetcher and returns its result`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1, 2, 3))

        val result = provider(A).getCrl(A)

        assertEquals(Available(byteArrayOf(1, 2, 3)), result)
        assertEquals(listOf(FakeCrlFetcher.Call(A, forced = false)), fakeFetcher.calls)
    }

    @Test
    fun `getCrl forwards forceRevalidation to the fetcher`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1))

        provider(A).getCrl(A, forceRevalidation = true)

        assertTrue(fakeFetcher.calls.last().forced)
    }

    @Test
    fun `getCrl accepts URLs not in the configured list`() = runBlocking {
        fakeFetcher.results[DISCOVERED] = Available(byteArrayOf(9))

        val result = provider(A).getCrl(DISCOVERED)

        assertEquals(Available(byteArrayOf(9)), result)
        assertEquals(DISCOVERED, fakeFetcher.calls.single().url)
    }


    @Test
    fun `empty prefetchURLs returns READY without any network request`() = runBlocking {
        assertEquals(READY, provider().refresh())
        assertTrue(fakeFetcher.calls.isEmpty())
    }

    @Test
    fun `all URLs usable returns READY`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1))
        fakeFetcher.results[B] = Available(byteArrayOf(2))
        fakeInspector.assessments[A] = USABLE
        fakeInspector.assessments[B] = USABLE

        assertEquals(READY, provider(A, B).refresh())
    }

    @Test
    fun `one unavailable URL returns UPDATE_REQUIRED`() = runBlocking {
        fakeFetcher.results[A] = Unavailable(CrlLookupFailure.NETWORK)
        fakeInspector.assessments[A] = UNAVAILABLE
        fakeFetcher.results[B] = Available(byteArrayOf(2))
        fakeInspector.assessments[B] = USABLE

        assertEquals(UPDATE_REQUIRED, provider(A, B).refresh())
    }


    @Test
    fun `all configured URLs are requested even when one fails`() = runBlocking {
        fakeFetcher.results[A] = Unavailable(CrlLookupFailure.NETWORK)
        fakeInspector.assessments[A] = UNAVAILABLE
        fakeFetcher.results[B] = Available(byteArrayOf(2))
        fakeInspector.assessments[B] = USABLE

        provider(A, B).refresh()

        assertEquals(setOf(A, B), fakeFetcher.calls.map { it.url }.toSet())
    }


    @Test
    fun `expired CRL triggers exactly one forced revalidation`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1))
        fakeInspector.assessmentSequence[A] = mutableListOf(EXPIRED, USABLE)

        assertEquals(READY, provider(A).refresh())
        assertEquals(2, fakeFetcher.calls.size)
        assertEquals(false, fakeFetcher.calls[0].forced)
        assertEquals(true, fakeFetcher.calls[1].forced)
    }

    @Test
    fun `still expired after forced revalidation returns UPDATE_REQUIRED`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1))
        fakeInspector.assessmentSequence[A] = mutableListOf(EXPIRED, EXPIRED)

        assertEquals(UPDATE_REQUIRED, provider(A).refresh())
        assertEquals(1, fakeFetcher.calls.count { it.forced })
    }

    @Test
    fun `unavailable result does not trigger a forced revalidation`() = runBlocking {
        fakeFetcher.results[A] = Unavailable(CrlLookupFailure.NETWORK)
        fakeInspector.assessments[A] = UNAVAILABLE

        provider(A).refresh()

        assertTrue(fakeFetcher.calls.none { it.forced })
    }


    @Test
    fun `URLs fetched via getCrl are not included in refresh readiness`() = runBlocking {
        fakeFetcher.results[A] = Available(byteArrayOf(1))
        fakeInspector.assessments[A] = USABLE
        fakeFetcher.results[DISCOVERED] = Unavailable(CrlLookupFailure.NETWORK)
        val p = provider(A)

        p.getCrl(DISCOVERED) // discovered during CoseVerification, not configured

        assertEquals(READY, p.refresh()) // only A is assessed
        assertEquals(listOf(DISCOVERED, A), fakeFetcher.calls.map { it.url })
    }

    @Test
    fun `cancellation from fetcher propagates out of refresh`() {
        fakeFetcher.failures[A] = CancellationException("cancelled")

        assertThrows(CancellationException::class.java) {
            runBlocking { provider(A).refresh() }
        }
    }

    private companion object {
        const val A = "http://dvs.verifier-ca.integration.account.gov.uk/crl/a.crl"
        const val B = "http://root.verifier-root-ca.integration.account.gov.uk/crl/b.crl"
        const val DISCOVERED = "http://dvs.l4.integration.account.gov.uk/crl/l4.crl"
    }

    private class FakeCrlFetcher : CrlFetcher {
        data class Call(val url: String, val forced: Boolean)

        val results = mutableMapOf<String, CrlLookup>()
        val failures = mutableMapOf<String, Throwable>()
        val calls = mutableListOf<Call>()

        override suspend fun getCrl(url: String, forceRevalidation: Boolean): CrlLookup {
            calls += Call(url, forceRevalidation)
            failures[url]?.let { throw it }
            return results[url] ?: Unavailable(CrlLookupFailure.NETWORK)
        }
    }

    private class FakeCrlCacheInspector : CrlCacheInspector {
        val assessments = mutableMapOf<String, CrlAssessment>()
        val assessmentSequence = mutableMapOf<String, MutableList<CrlAssessment>>()

        override suspend fun assess(url: String, lookup: CrlLookup): CrlAssessment {
            assessmentSequence[url]?.let { seq ->
                if (seq.isNotEmpty()) return seq.removeFirst()
            }
            return assessments[url] ?: UNAVAILABLE
        }
    }
}