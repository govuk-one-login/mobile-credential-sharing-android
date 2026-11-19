package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.android.network.client.v3.RawHttpResponse

internal class FakeRawHttpClient : RawHttpClient {
    var response: RawHttpResponse? = null
    var failure: Throwable? = null
    val responsesByUrl = mutableMapOf<String, RawHttpResponse>()
    val failuresByUrl = mutableMapOf<String, IOException>()
    val requestedUrls = mutableListOf<String>()
    var requestedForce: Boolean? = null

    override suspend fun get(
        url: String,
        headers: List<Pair<String, String>>,
        forceRevalidation: Boolean
    ): RawHttpResponse {
        requestedUrls += url
        requestedForce = forceRevalidation
        failuresByUrl[url]?.let { throw it }
        failure?.let { throw it }
        return responsesByUrl[url] ?: checkNotNull(response)
    }

    override suspend fun cached(url: String): RawHttpResponse? = null
}
