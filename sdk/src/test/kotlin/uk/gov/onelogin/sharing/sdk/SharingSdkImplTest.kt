package uk.gov.onelogin.sharing.sdk

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger

class SharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var sdk: CredentialSharingSdkImpl

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
