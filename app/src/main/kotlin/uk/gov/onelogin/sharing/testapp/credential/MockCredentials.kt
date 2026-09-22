package uk.gov.onelogin.sharing.testapp.credential

import uk.gov.onelogin.sharing.testapp.R

object MockCredentials {

    fun getMockCredentialStates(): List<MockCredentialState> = listOf(
        MockCredentialState(
            displayName = "Jane Doe"
        ),
        MockCredentialState(
            displayName = "Jane Doe (absent x5t)",
            rawCredentialRes = R.raw.mock_credential_absent_x5t
        ),
        MockCredentialState(
            displayName = "Jane Doe (signing failure)",
            providerType = MockCredentialProviderType.SIGNING_FAILURE
        ),
        MockCredentialState(
            displayName = "Jane Doe (authentication cancelled once)",
            providerType = MockCredentialProviderType.AUTH_CANCELLED_ONCE
        )
    )
}
