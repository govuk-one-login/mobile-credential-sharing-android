package uk.gov.onelogin.sharing

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk
import uk.gov.onelogin.sharing.di.internal.shared.CredentialSharingSdkImpl
import uk.gov.onelogin.sharing.di.internal.verifier.VerifierCredentialSdkImpl

class VerifierSharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var credentialSharingSdk: CredentialSharingSdk
    private lateinit var verifierCredentialSdk: VerifierCredentialSdk

    @Before
    fun setUp() {
        logger = SystemLogger()

        credentialSharingSdk = CredentialSharingSdkImpl(
            logger = logger,
            applicationContext = mockk()
        )

        verifierCredentialSdk = VerifierCredentialSdkImpl(credentialSharingSdk.appGraph)
    }

    @Test
    fun `SDK is successfully initialized`() {
        assertNotNull(credentialSharingSdk)
        assertNotNull(verifierCredentialSdk)
    }
}
