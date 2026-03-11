package uk.gov.onelogin.orchestration

interface CredentialProviderNew {
    fun provideCredential()
}

class CredentialProviderNewImpl : CredentialProviderNew{
    override fun provideCredential() {
        println("Providing credential")
    }
}