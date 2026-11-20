package uk.gov.onelogin.sharing.verification.crl

import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.client.v3.RawHttpResponse

/**
 * Test double for [RawHttpClient].
 *
 * Simulates both the network transport and an in-memory cache via [stored]. Calls that succeed
 * with a 200 are automatically placed in [stored] when [retainResponses] is true, mirroring
 * what Ktor's persistent cache does. Forced responses can be pre-loaded via [forcedResponses].
 *
 * HTTP freshness and stale-fallback behaviour belong to the network library and are tested there.
 */
internal class FakeRawHttpClient : RawHttpClient {

    data class Request(val url: String, val forced: Boolean)

    /** Pre-loaded responses returned on a normal (non-forced) GET. */
    val responses = mutableMapOf<String, RawHttpResponse>()

    /** Pre-loaded responses returned only when forceRevalidation = true. */
    val forcedResponses = mutableMapOf<String, RawHttpResponse>()

    /** Exceptions thrown instead of returning a response. */
    val failures = mutableMapOf<String, Throwable>()

    /** Simulated cache store. Populated automatically on 200 responses when [retainResponses]. */
    val stored = mutableMapOf<String, RawHttpResponse>()

    /** Recorded calls in order. */
    val requests = mutableListOf<Request>()

    /** Set to false to simulate a `no-store` response that the cache does not keep. */
    var retainResponses = true

    val requestedUrls: List<String> get() = requests.map { it.url }

    override suspend fun get(
        url: String,
        headers: List<Pair<String, String>>,
        forceRevalidation: Boolean,
    ): RawHttpResponse {
        requests += Request(url, forceRevalidation)
        failures[url]?.let { throw it }
        val response = (if (forceRevalidation) forcedResponses[url] else null)
            ?: checkNotNull(responses[url]) { "FakeRawHttpClient: no response configured for $url" }
        if (retainResponses && response.status == 200) stored[url] = response
        return response
    }

    override suspend fun cached(url: String): RawHttpResponse? = stored[url]
}
