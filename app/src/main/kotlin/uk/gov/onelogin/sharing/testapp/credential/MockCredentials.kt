package uk.gov.onelogin.sharing.testapp.credential

object MockCredentials {

    fun getMockCredentialStates(): List<MockCredentialState> = listOf(
        MockCredentialState(
            displayName = "Jane Doe"
        )
    )
}
