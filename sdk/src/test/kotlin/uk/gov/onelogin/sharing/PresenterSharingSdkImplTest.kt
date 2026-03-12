package uk.gov.onelogin.sharing

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.internal.presenter.PresenterCredentialSdkImpl
import uk.gov.onelogin.sharing.di.internal.shared.CredentialSharingSdkImpl

class PresenterSharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var credentialSharingSdk: CredentialSharingSdk
    private lateinit var presenterCredentialSdk: PresenterCredentialSdk

    @Before
    fun setUp() {
        logger = SystemLogger()

        credentialSharingSdk = CredentialSharingSdkImpl(
            logger = logger,
            applicationContext = mockk()
        )

        presenterCredentialSdk = PresenterCredentialSdkImpl(credentialSharingSdk.appGraph)
    }

    @Test
    fun `SDK is successfully initialized`() {
        assertNotNull(credentialSharingSdk)
        assertNotNull(presenterCredentialSdk)
    }
}
