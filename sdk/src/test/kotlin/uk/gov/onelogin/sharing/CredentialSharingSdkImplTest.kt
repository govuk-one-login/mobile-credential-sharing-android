package uk.gov.onelogin.sharing

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.internal.shared.CredentialSharingSdkImpl

class CredentialSharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var sdk: CredentialSharingSdk

    @Before
    fun setUp() {
        logger = SystemLogger()

        sdk = CredentialSharingSdkImpl(
            logger = logger,
            applicationContext = mockk()
        )
    }

    @Test
    fun `SDK is successfully initialized`() {
        assertNotNull(sdk)
    }
}
