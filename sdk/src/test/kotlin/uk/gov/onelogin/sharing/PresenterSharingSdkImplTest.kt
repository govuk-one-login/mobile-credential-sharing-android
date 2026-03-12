package uk.gov.onelogin.sharing

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.internal.presenter.CredentialPresenterImpl
import uk.gov.onelogin.sharing.di.internal.presenter.PresenterCredentialSdkImpl
import uk.gov.onelogin.sharing.sdk.FakeCredentialProvider

class PresenterSharingSdkImplTest {
    private val appGraph = mockk<CredentialSharingAppGraph>()
    private val presenterGraphFactory = mockk<PresenterCredentialGraph.Factory>()
    private val holderGraph = mockk<PresenterCredentialGraph>()
    private val orchestrator = mockk<Orchestrator.Holder>()

    @Test
    fun `verifier returns CredentialHolder with expected dependencies`() {
        val credentialProvider = FakeCredentialProvider()

        every { presenterGraphFactory.create(appGraph, credentialProvider) } returns holderGraph
        every { holderGraph.holderOrchestrator() } returns orchestrator

        val sdk = PresenterCredentialSdkImpl(
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
