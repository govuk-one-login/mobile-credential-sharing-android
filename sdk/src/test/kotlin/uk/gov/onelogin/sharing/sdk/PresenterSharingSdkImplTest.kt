package uk.gov.onelogin.sharing.sdk

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.internal.presenter.CredentialPresenterImpl
import uk.gov.onelogin.sharing.sdk.internal.presenter.PresentCredentialSdkImpl

class PresenterSharingSdkImplTest {
    private val appGraph = mockk<CredentialSharingAppGraph>()
    private val presenterGraphFactory = mockk<PresentCredentialGraph.Factory>()
    private val holderGraph = mockk<PresentCredentialGraph>()
    private val orchestrator = mockk<Orchestrator.Holder>()

    @Test
    fun `holder returns CredentialHolder with expected dependencies`() {
        val credentialProvider = FakeCredentialProvider()

        every { presenterGraphFactory.create(appGraph, credentialProvider) } returns holderGraph
        every { holderGraph.holderOrchestrator() } returns orchestrator

        val sdk = PresentCredentialSdkImpl(
            appGraph = appGraph,
            presenterGraphFactory = presenterGraphFactory
        )

        val result = sdk.presenter(credentialProvider = credentialProvider)

        assertTrue(result is CredentialPresenterImpl)

        result as CredentialPresenterImpl
        assertSame(appGraph, result.appGraph)
        assertSame(orchestrator, result.orchestrator)
    }
}
