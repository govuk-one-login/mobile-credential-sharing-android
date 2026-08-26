package uk.gov.onelogin.sharing.sdk

import io.mockk.mockk
import kotlin.test.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

class CredentialSharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var sdk: CredentialSharingSdk
    private val factory: ReaderAuthCredentialProvider.Factory = mockk(relaxed = true)

    @Before
    fun setUp() {
        logger = SystemLogger()

        sdk = CredentialSharingSdkImpl(
            logger = logger,
            applicationContext = mockk(),
            permissionChecker = { emptyList() },
            readerAuthCredentialFactory = factory
        )
    }

    @Test
    fun `SDK is successfully initialized`() {
        assertNotNull(sdk)
    }
}
