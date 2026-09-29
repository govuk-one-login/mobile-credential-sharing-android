package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class ReaderAuthOptionTest {
    @Test
    fun `Mocked options share the upper chain and vary the leaf`() = runTest {
        val mocked = mapOf(
            ReaderAuthOption.VALID to "reader_valid_x509_leaf_certificate",
            ReaderAuthOption.INVALID_NAME_CONSTRAINTS to
                "reader_x509_leaf_with_invalid_organisation",
            ReaderAuthOption.INVALID_MISSING_PRIVACY_POLICY to
                "reader_x509_leaf_without_privacy_policy"
        )

        mocked.forEach { (option, leaf) ->
            assertEquals(
                listOf(
                    "test_reader_auth_x509_certificate.der",
                    "test_reader_auth_name_constrained_x509_certificate.der",
                    "$leaf.der"
                ),
                option.certificateChain
            )
            assertEquals(
                listOf(
                    "test_reader_auth_x509_certificate.pem",
                    "test_reader_auth_name_constrained_x509_certificate.pem",
                    "$leaf.pem"
                ),
                option.privateKeyChain
            )
        }
    }

    @Test
    fun `DVS options use a single chain file and a single leaf key`() = runTest {
        assertEquals(
            listOf("reader_dvs_dev_chain.der"),
            ReaderAuthOption.DVS_DEV.certificateChain
        )
        assertEquals(
            listOf("reader_dvs_dev_leaf_key.pem"),
            ReaderAuthOption.DVS_DEV.privateKeyChain
        )

        assertEquals(
            listOf("reader_dvs_integration_chain.der"),
            ReaderAuthOption.DVS_INTEGRATION.certificateChain
        )
        assertEquals(
            listOf("reader_dvs_integration_leaf_key.pem"),
            ReaderAuthOption.DVS_INTEGRATION.privateKeyChain
        )
    }

    @Test
    fun `Leaf certificate asset is the last certificate in the chain`() = runTest {
        assertEquals(
            "reader_dvs_dev_chain.der",
            ReaderAuthOption.DVS_DEV.leafCertificateAsset
        )
        assertEquals(
            "reader_valid_x509_leaf_certificate.der",
            ReaderAuthOption.VALID.leafCertificateAsset
        )
    }
}
