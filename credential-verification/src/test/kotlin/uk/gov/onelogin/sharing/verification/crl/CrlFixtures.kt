package uk.gov.onelogin.sharing.verification.crl

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v2CRLBuilder
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import uk.gov.android.network.client.v3.RawHttpResponse

/**
 * CRL test fixtures. BouncyCastle is used here only for generating DER bytes;
 * production code uses only the JDK.
 */
internal object CrlFixtures {
    const val SECOND = 1_000L
    const val DAY = 24 * 60 * 60 * SECOND

    /** A fixed "now" for deterministic date tests. */
    const val NOW = 1_800_000_000_000L

    private val signingKeyPair by lazy {
        KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp384r1")) }
            .generateKeyPair()
    }

    /** Builds a real DER X.509 v2 CRL. `nextUpdate` may be null for edge-case tests. */
    fun crl(thisUpdate: Long, nextUpdate: Long?): ByteArray {
        val builder = X509v2CRLBuilder(X500Name("CN=Test CRL Issuer"), Date(thisUpdate))
        nextUpdate?.let { builder.setNextUpdate(Date(it)) }
        val holder = builder.build(
            JcaContentSignerBuilder("SHA384withECDSA").build(signingKeyPair.private)
        )
        return JcaX509CRLConverter().getCRL(holder).encoded
    }

    /** Valid at [NOW]: issued one day ago, expires in six days. */
    fun validCrl() = crl(thisUpdate = NOW - DAY, nextUpdate = NOW + 6 * DAY)

    /** Already expired: nextUpdate was one day ago. */
    fun expiredCrl() = crl(thisUpdate = NOW - 8 * DAY, nextUpdate = NOW - DAY)

    /** A 200 [RawHttpResponse] with the given body and a one-hour max-age header. */
    fun ok(body: ByteArray) =
        RawHttpResponse(200, mapOf("Cache-Control" to listOf("max-age=3600")), body)

    /** A non-success [RawHttpResponse] with an empty body. */
    fun errorResponse(code: Int) = RawHttpResponse(code, emptyMap(), ByteArray(0))
}