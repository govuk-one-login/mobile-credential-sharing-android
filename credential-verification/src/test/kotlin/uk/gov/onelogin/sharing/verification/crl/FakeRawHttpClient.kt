package uk.gov.onelogin.sharing.verification.crl

import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.client.v3.RawHttpResponse

/**
 * Stands in for the network library's raw client. Freshness and stale-fallback behaviour belong to
 * the library and are tested there. This fake only simulates a cache that does, or does not,
 * retain successful responses.
 */
internal class FakeRawHttpClient : RawHttpClient {
    data class Request(val url: String, val forced: Boolean)

    val responses = mutableMapOf<String, RawHttpResponse>()
    val forcedResponses = mutableMapOf<String, RawHttpResponse>()
    val failures = mutableMapOf<String, Throwable>()
    val stored = mutableMapOf<String, RawHttpResponse>()
    val requests = mutableListOf<Request>()
    var retainResponses = true

    val requestedUrls get() = requests.map { it.url }

    override suspend fun get(
        url: String,
        headers: List<Pair<String, String>>,
        forceRevalidation: Boolean
    ): RawHttpResponse {
        requests += Request(url, forceRevalidation)
        failures[url]?.let { throw it }
        val response = (if (forceRevalidation) forcedResponses[url] else null)
            ?: checkNotNull(responses[url]) { "no response configured for $url" }
        if (retainResponses && response.status == 200) stored[url] = response
        return response
    }

    override suspend fun cached(url: String): RawHttpResponse? = stored[url]
}
