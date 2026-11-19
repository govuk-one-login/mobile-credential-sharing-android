package uk.gov.onelogin.sharing.verification.crl

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.X509v2CRLBuilder
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import uk.gov.android.network.client.v3.RawHttpResponse

/** Generated with BouncyCastle, which is test-only. Dates are whole seconds, as in a real CRL. */
internal object CrlFixtures {
    const val SECOND = 1_000L
    const val DAY = 24 * 60 * 60 * SECOND

    /** A fixed "now" for tests, aligned to a whole second. */
    const val NOW = 1_800_000_000_000L

    fun crl(thisUpdate: Long, nextUpdate: Long?): ByteArray {
        val keys = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp384r1")) }
            .generateKeyPair()
        val builder = X509v2CRLBuilder(X500Name("CN=Test CRL Issuer"), Date(thisUpdate))
        nextUpdate?.let { builder.setNextUpdate(Date(it)) }
        val holder = builder.build(JcaContentSignerBuilder("SHA384withECDSA").build(keys.private))
        return JcaX509CRLConverter().getCRL(holder).encoded
    }

    /** Valid at [NOW]: issued a day ago, next update in six days. */
    fun validCrl() = crl(thisUpdate = NOW - DAY, nextUpdate = NOW + 6 * DAY)

    fun expiredCrl() = crl(thisUpdate = NOW - 8 * DAY, nextUpdate = NOW - DAY)

    fun ok(body: ByteArray) =
        RawHttpResponse(200, mapOf("Cache-Control" to listOf("max-age=3600")), body)

    fun status(code: Int) = RawHttpResponse(code, emptyMap(), ByteArray(0))
}
