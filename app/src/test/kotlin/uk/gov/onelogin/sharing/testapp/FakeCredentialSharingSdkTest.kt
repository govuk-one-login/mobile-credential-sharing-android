@file:Suppress("DEPRECATION")

package uk.gov.onelogin.sharing.testapp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertSame
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

class FakeCredentialSharingSdkTest {
    private val appGraph: CredentialSharingAppGraph = mockk()
    @Suppress("DEPRECATION")
    private val presentCredentialSdk: PresentCredentialSdk = mockk(relaxed = true)
    private val verifyCredentialSdk: VerifyCredentialSdk = mockk()

    @Test
    fun `createCredentialPresenter uses injected credentialPresenter if provided`() {
        val customPresenter: CredentialPresenter = mockk()
        val provider: CredentialProvider = mockk()

        val fakeSdk = FakeCredentialSharingSdk(
            appGraph = appGraph,
            presentCredentialSdk = presentCredentialSdk,
            verifyCredentialSdk = verifyCredentialSdk,
            credentialPresenter = customPresenter,
        )

        val result = fakeSdk.createCredentialPresenter(provider, emptyList())
        assertSame(customPresenter, result)
    }

    @Test
    fun `createCredentialPresenter falls back to presentCredentialSdk if null`() {
        val expectedPresenter: CredentialPresenter = mockk()
        val provider: CredentialProvider = mockk()
        @Suppress("DEPRECATION")
        every { presentCredentialSdk.presenter(provider) } returns expectedPresenter

        val fakeSdk = FakeCredentialSharingSdk(
            appGraph = appGraph,
            presentCredentialSdk = presentCredentialSdk,
            verifyCredentialSdk = verifyCredentialSdk,
            credentialPresenter = null,
        )

        val result = fakeSdk.createCredentialPresenter(provider, emptyList())
        assertSame(expectedPresenter, result)
        @Suppress("DEPRECATION")
        verify { presentCredentialSdk.presenter(provider) }
    }
}
