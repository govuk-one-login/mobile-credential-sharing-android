package uk.gov.onelogin.sharing.sdk

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

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
