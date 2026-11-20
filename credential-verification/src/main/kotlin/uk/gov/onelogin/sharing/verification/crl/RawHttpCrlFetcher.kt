package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import uk.gov.android.network.client.v3.RawHttpClient
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Available
import uk.gov.onelogin.sharing.verification.crl.CrlLookup.Unavailable

/** Adapts [RawHttpClient] responses and transport errors into [CrlLookup] outcomes. */
internal class RawHttpCrlFetcher(
    private val client: RawHttpClient
) : CrlFetcher {

    override suspend fun getCrl(url: String, forceRevalidation: Boolean): CrlLookup {
        val response = try {
            client.get(url = url, forceRevalidation = forceRevalidation)
        } catch (_: IOException) {
            return Unavailable(CrlLookupFailure.NETWORK)
        }
        return if (response.status == HTTP_OK) {
            Available(response.body.copyOf())
        } else {
            Unavailable(CrlLookupFailure.RESPONSE)
        }
    }

    private companion object {
        const val HTTP_OK = 200
    }
}