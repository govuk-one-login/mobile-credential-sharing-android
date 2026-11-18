package uk.gov.onelogin.sharing.verification.crl

import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.cert.CRLException
import java.security.cert.CertificateFactory
import java.security.cert.X509CRL
import uk.gov.android.network.api.v2.ApiRequest
import uk.gov.android.network.client.v2.GenericHttpClient
import uk.gov.android.network.client.v2.GenericResponseException

/**
 * Spike: fetches a CRL through the organisation's network library
 * ([GenericHttpClient], the low-level layer of `uk.gov.android:network`).
 *
 * Only the public, unauthenticated GET path is used. Auth, attestation and DPoP are
 * deliberately not involved.
 *
 * The library's [uk.gov.android.network.client.v2.GenericHttpResponse] exposes the body as a
 * [String] and exposes no headers, so the bytes are recovered here as ISO-8859-1. That is only
 * lossless if the library decodes the body as ISO-8859-1, which is not guaranteed. See
 * [CrlFetchResult.Malformed].
 */
internal class CrlEndpointClient(private val httpClient: GenericHttpClient) {

    suspend fun fetch(url: String): CrlFetchResult {
        val response = try {
            httpClient.request(ApiRequest.Get(url = url))
        } catch (e: GenericResponseException) {
            return CrlFetchResult.HttpError(status = e.response.status)
        } catch (e: IOException) {
            return CrlFetchResult.Unreachable(cause = e)
        }

        val bytes = response.body.toByteArray(Charsets.ISO_8859_1)
        return try {
            val crl = CertificateFactory.getInstance("X.509")
                .generateCRL(ByteArrayInputStream(bytes)) as X509CRL
            CrlFetchResult.Success(status = response.status, crl = crl, byteCount = bytes.size)
        } catch (e: CRLException) {
            CrlFetchResult.Malformed(
                status = response.status,
                byteCount = bytes.size,
                cause = e
            )
        }
    }
}

internal sealed interface CrlFetchResult {
    /** HTTP 2xx and the body parsed as a DER X.509 CRL. */
    data class Success(val status: Int, val crl: X509CRL, val byteCount: Int) : CrlFetchResult

    /**
     * HTTP 2xx but the body did not parse as a CRL. With the current library this is the
     * expected outcome when the String decoding was not byte-preserving.
     */
    data class Malformed(val status: Int, val byteCount: Int, val cause: Throwable) :
        CrlFetchResult

    /** Non-success status (3xx, 4xx, 5xx). */
    data class HttpError(val status: Int) : CrlFetchResult

    /** Connection-level failure. */
    data class Unreachable(val cause: IOException) : CrlFetchResult
}
