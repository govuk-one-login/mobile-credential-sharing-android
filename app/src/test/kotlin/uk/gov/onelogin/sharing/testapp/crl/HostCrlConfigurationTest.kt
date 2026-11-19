package uk.gov.onelogin.sharing.testapp.crl

import androidx.test.core.app.ApplicationProvider
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.verification.crl.CrlConfiguration

/**
 * Mimics the host app handing the SDK its CRL configuration: the SDK reads the host's
 * `assets/credential-sharing-crl.json` through the application context.
 */
@RunWith(RobolectricTestRunner::class)
class HostCrlConfigurationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `sdk receives the urls the host bundled`() {
        val config = CrlConfiguration.load(context)

        assertEquals(
            listOf(
                "http://dvs.verifier-ca.integration.account.gov.uk/crl/" +
                    "85b2ae92-97ab-406b-93b2-f765795de390.crl",
                "http://root.verifier-root-ca.integration.account.gov.uk/crl/" +
                    "3689017a-d5f2-4762-b518-afb86983dd25.crl"
            ),
            config.prefetchUrls
        )
    }

    @Test
    fun `host urls are the distribution points in the integration reader chain`() {
        val chain = context.assets.open("reader_dvs_integration_chain.der").use {
            CertificateFactory.getInstance("X.509").generateCertificates(it)
        }.filterIsInstance<X509Certificate>()
        // The tracked asset is a placeholder until CI (or a local run of the provision-reader-cert
        // action) replaces it with a real chain. Skip, rather than fail, when it is not provisioned.
        assumeTrue("reader_dvs_integration_chain.der is a placeholder, not a real chain", chain.isNotEmpty())
        // CRL Distribution Points extension, which holds each URL as ASCII inside the DER.
        val distributionPoints = chain
            .mapNotNull { it.getExtensionValue(CRL_DISTRIBUTION_POINTS_OID) }
            .map { String(it, Charsets.ISO_8859_1) }

        CrlConfiguration.load(context).prefetchUrls.forEach { url ->
            assertTrue(
                distributionPoints.any { url in it },
                "$url is not a CRL distribution point in the chain"
            )
        }
    }

    private companion object {
        const val CRL_DISTRIBUTION_POINTS_OID = "2.5.29.31"
    }
}
