package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.CredentialProviderNew

class FakeCredentialProviderNew : CredentialProviderNew {
    override fun provideCredential() {
        println("providing fake credential")
    }
}