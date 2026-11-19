package uk.gov.onelogin.sharing.verification.crl

import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.cert.CRLException
import java.security.cert.CertificateFactory
import java.security.cert.X509CRL
import uk.gov.android.network.client.v3.RawHttpClient

/**
 * Spike: fetches a CRL through the organisation's network library.
 *
 * Uses the library's [RawHttpClient] (`uk.gov.android:network`, `client.v3`), which returns the
 * body as unmodified bytes, exposes the response headers and keeps a persistent HTTP cache.
 * Authentication, attestation and DPoP are not involved.
 *
 * This class only locates and parses the bytes. Issuer, signature, coverage, date and
 * revocation-entry checks belong to CoseVerification.
 */
internal class CrlEndpointClient(private val httpClient: RawHttpClient) {

    suspend fun fetch(url: String, forceRevalidation: Boolean = false): CrlFetchResult {
        val response = try {
            httpClient.get(url = url, forceRevalidation = forceRevalidation)
        } catch (e: IOException) {
            return CrlFetchResult.Unreachable(cause = e)
        }

        if (response.status !in SUCCESS_RANGE) {
            return CrlFetchResult.HttpError(status = response.status)
        }

        return try {
            val crl = CertificateFactory.getInstance("X.509")
                .generateCRL(ByteArrayInputStream(response.body)) as X509CRL
            CrlFetchResult.Success(
                status = response.status,
                crl = crl,
                byteCount = response.body.size,
                cacheControl = response.header("Cache-Control")
            )
        } catch (e: CRLException) {
            CrlFetchResult.Malformed(
                status = response.status,
                byteCount = response.body.size,
                cause = e
            )
        }
    }

    private companion object {
        val SUCCESS_RANGE = 200..299
    }
}

sealed interface CrlFetchResult {
    /** HTTP 2xx and the body parsed as a DER X.509 CRL. */
    data class Success(
        val status: Int,
        val crl: X509CRL,
        val byteCount: Int,
        val cacheControl: String?
    ) : CrlFetchResult

    /** HTTP 2xx but the body did not parse as a DER X.509 CRL. */
    data class Malformed(val status: Int, val byteCount: Int, val cause: Throwable) :
        CrlFetchResult

    /** Non-success status (3xx, 4xx, 5xx). */
    data class HttpError(val status: Int) : CrlFetchResult

    /** Connection-level failure, including timeouts. */
    data class Unreachable(val cause: IOException) : CrlFetchResult
}
