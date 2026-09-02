package uk.gov.onelogin.sharing.sdk

import io.mockk.every
import io.mockk.mockk
import java.security.cert.X509Certificate
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph
import uk.gov.onelogin.sharing.sdk.internal.verifier.CredentialVerifierImpl
import uk.gov.onelogin.sharing.sdk.internal.verifier.VerifyCredentialSdkImpl
import uk.gov.onelogin.sharing.verification.CredentialVerificationGraph

class VerifierSharingSdkImplTest {
    private val appGraph = mockk<CredentialSharingAppGraph>()
    private val logger = SystemLogger()
    private val verifierGraphFactory = mockk<VerifyCredentialGraph.Factory>()
    private val readerAuthCredentialProvider: ReaderAuthCredentialProvider = mockk(relaxed = true)
    private val credentialVerificationFactory = mockk<CredentialVerificationGraph.Factory>(
        relaxed = true
    )
    private val verifierGraph = mockk<VerifyCredentialGraph>()
    private val credentialVerificationGraph = mockk<CredentialVerificationGraph>()
    private val orchestrator = mockk<Orchestrator.Verifier>()

    @Test
    fun `verifier returns CredentialVerifier with expected dependencies`() {
        val verificationRequest = VerificationRequest(
            documentType = "org.iso.18013.5.1.mDL",
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.GivenName to true,
                    MdlAttribute.FamilyName to true
                )
            )
        )
        val trustedRootCertificate: X509Certificate = mockk()
        val verifierConfig = VerifierConfig(
            verificationRequest = verificationRequest,
            trustedRootCertificate = trustedRootCertificate
        )

        every {
            appGraph.logger()
        } returns logger
        every {
            credentialVerificationFactory.create(trustedRootCertificate)
        } returns credentialVerificationGraph

        every {
            verifierGraphFactory.create(
                appGraph = appGraph,
                credentialVerificationGraph = credentialVerificationGraph,
                verifierConfig = verifierConfig,
                readerAuthCredentialProvider = readerAuthCredentialProvider
            )
        } returns verifierGraph
        every { verifierGraph.verifierOrchestrator() } returns orchestrator

        val sdk = VerifyCredentialSdkImpl(
            appGraph = appGraph,
            verifierGraphFactory = verifierGraphFactory,
            credentialVerificationGraphFactory = credentialVerificationFactory,
            readerAuthCredentialFactory = {
                readerAuthCredentialProvider
            }
        )

        val result = sdk.verifier(verifierConfig)

        assertTrue(result is CredentialVerifierImpl)

        result as CredentialVerifierImpl
        assertSame(appGraph, result.appGraph)
        assertSame(orchestrator, result.orchestrator)
    }
}
