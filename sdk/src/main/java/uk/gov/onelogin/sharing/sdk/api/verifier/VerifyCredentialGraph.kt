package uk.gov.onelogin.sharing.sdk.api.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.auth.reader.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.verification.CredentialVerificationGraph

@DependencyGraph(AppScope::class)
interface VerifyCredentialGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Includes credentialVerificationGraph: CredentialVerificationGraph,
            @Provides verifierConfig: VerifierConfig,
            @Provides readerAuthCredentialProvider: ReaderAuthCredentialProvider,
        ): VerifyCredentialGraph
    }

    fun verifierOrchestrator(): Orchestrator.Verifier

    fun verifierConfig(): VerifierConfig

    fun readerAuthCredentialProvider(): ReaderAuthCredentialProvider
}
