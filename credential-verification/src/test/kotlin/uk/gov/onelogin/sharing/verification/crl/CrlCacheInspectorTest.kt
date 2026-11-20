package uk.gov.onelogin.sharing.verification.crl

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.android.network.client.v3.RawHttpResponse
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.EXPIRED
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.NOT_RETAINED
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.NOT_YET_VALID
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.UNAVAILABLE
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.UNREADABLE
import uk.gov.onelogin.sharing.verification.crl.CrlAssessment.USABLE
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.DAY
import uk.gov.onelogin.sharing.verification.crl.CrlFixtures.NOW
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Unavailable

/**
 * Unit tests for [DefaultCrlCacheInspector].
 *
 * The inspector is decoupled from real CRL bytes via the [CrlValidityDecoder] seam, so these
 * tests use a simple lambda decoder to control validity windows without building DER fixtures.
 * [FakeRawHttpClient] stands in for the cache.
 */
class CrlCacheInspectorTest {

    private val fakeClient = FakeRawHttpClient()

    private fun inspector(
        validity: CrlValidity? = CrlValidity(thisUpdate = NOW - DAY, nextUpdate = NOW + 6 * DAY),
        now: Long = NOW,
    ): DefaultCrlCacheInspector {
        val decoder = CrlValidityDecoder { _ -> validity }
        return DefaultCrlCacheInspector(
            client = fakeClient,
            decoder = decoder,
            timeProvider = TimeProvider { now },
        )
    }

    @Test
    fun `Unavailable lookup returns UNAVAILABLE without touching the cache`() = runBlocking {
        val result = inspector().assess(URL, Unavailable(CrlLookupFailure.NETWORK))

        assertEquals(UNAVAILABLE, result)
    }

    @Test
    fun `nothing cached after a successful lookup returns NOT_RETAINED`() = runBlocking {
        val result = inspector().assess(URL, Available(byteArrayOf(1)))

        assertEquals(NOT_RETAINED, result)
    }

    @Test
    fun `cached entry with valid dates returns USABLE`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))

        assertEquals(USABLE, inspector().assess(URL, Available(byteArrayOf(1))))
    }

    @Test
    fun `cached but undecodable bytes return UNREADABLE`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))

        assertEquals(UNREADABLE, inspector(validity = null).assess(URL, Available(byteArrayOf(1))))
    }

    @Test
    fun `now exactly equals nextUpdate is EXPIRED`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))
        val expiredValidity = CrlValidity(thisUpdate = NOW - DAY, nextUpdate = NOW)

        assertEquals(
            EXPIRED,
            inspector(expiredValidity, now = NOW).assess(URL, Available(byteArrayOf(1)))
        )
    }

    @Test
    fun `now one millisecond before nextUpdate is USABLE`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))
        val validity = CrlValidity(thisUpdate = NOW - DAY, nextUpdate = NOW + 1)

        assertEquals(USABLE, inspector(validity, now = NOW).assess(URL, Available(byteArrayOf(1))))
    }

    @Test
    fun `future-dated thisUpdate returns NOT_YET_VALID`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))
        val futureValidity = CrlValidity(thisUpdate = NOW + DAY, nextUpdate = NOW + 7 * DAY)

        assertEquals(
            NOT_YET_VALID,
            inspector(futureValidity, now = NOW).assess(URL, Available(byteArrayOf(1)))
        )
    }

    @Test
    fun `now exactly equals thisUpdate is USABLE`() = runBlocking {
        fakeClient.stored[URL] = RawHttpResponse(200, emptyMap(), byteArrayOf(1))
        val validity = CrlValidity(thisUpdate = NOW, nextUpdate = NOW + DAY)

        assertEquals(USABLE, inspector(validity, now = NOW).assess(URL, Available(byteArrayOf(1))))
    }

    private companion object {
        const val URL = "http://dvs.verifier-ca.integration.account.gov.uk/crl/example.crl"
    }
}
