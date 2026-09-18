package uk.gov.onelogin.sharing.sdk

import android.bluetooth.BluetoothManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import java.security.cert.X509Certificate
import junit.framework.TestCase.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.v2.Logger
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.internal.presenter.CredentialPresenterImpl
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

class CredentialSharingSdkImplTest {
    private lateinit var logger: Logger
    private lateinit var sdk: CredentialSharingSdk
    private val factory: ReaderAuthCredentialProvider.Factory = mockk(relaxed = true)

    @Before
    fun setUp() {
        logger = SystemLogger()

        val mockBluetoothManager = mockk<BluetoothManager>(relaxed = true)
        val mockContext = mockk<Context>(relaxed = true) {
            every { getSystemService(Context.BLUETOOTH_SERVICE) } returns mockBluetoothManager
            every { getSystemService(BluetoothManager::class.java) } returns mockBluetoothManager
        }

        sdk = CredentialSharingSdkImpl(
            logger = logger,
            applicationContext = mockContext,
            permissionChecker = { emptyList() },
            readerAuthCredentialFactory = factory
        )
    }

    @Test
    fun `SDK is successfully initialized`() {
        assertNotNull(sdk)
        assertNotNull(sdk.appGraph)
        assertNotNull(sdk.verifyCredentialSdk)
    }

    @Test
    fun `legacy presentCredentialSdk returns CredentialPresenter`() {
        @Suppress("DEPRECATION")
        val presentSdk = sdk.presentCredentialSdk
        assertNotNull(presentSdk)

        val credentialProvider = FakeCredentialProvider()
        val presenter = presentSdk.presenter(credentialProvider)
        assertNotNull(presenter)
        assertTrue(presenter is CredentialPresenterImpl)
    }

    @Test
    fun `createCredentialPresenter returns CredentialPresenterImpl with default empty trusted certificates`() {
        val credentialProvider = FakeCredentialProvider()

        val presenter = sdk.createCredentialPresenter(
            credentialProvider = credentialProvider
        )

        assertNotNull(presenter)
        assertTrue(presenter is CredentialPresenterImpl)
        val impl = presenter as CredentialPresenterImpl
        assertSame(sdk.appGraph, impl.appGraph)
        assertNotNull(impl.orchestrator)
    }

    @Test
    fun `createCredentialPresenter returns CredentialPresenterImpl with trusted certificates list`() {
        val credentialProvider = FakeCredentialProvider()
        val trustedCerts = listOf<X509Certificate>(mockk())

        val presenter = sdk.createCredentialPresenter(
            credentialProvider = credentialProvider,
            trustedReaderCertificates = trustedCerts
        )

        assertNotNull(presenter)
        assertTrue(presenter is CredentialPresenterImpl)
        val impl = presenter as CredentialPresenterImpl
        assertSame(sdk.appGraph, impl.appGraph)
        assertNotNull(impl.orchestrator)
    }
}
