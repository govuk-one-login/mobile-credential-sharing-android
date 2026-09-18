package uk.gov.onelogin.sharing.testapp.credential

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MockCredentialsTest {

    private val states = MockCredentials.getMockCredentialStates()

    @Test
    fun `includes a signing-failure Jane Doe option`() {
        val state = states.firstOrNull { it.displayName == "Jane Doe (signing failure)" }

        assertNotNull(state)
        assertEquals(MockCredentialProviderType.SIGNING_FAILURE, state.providerType)
    }

    @Test
    fun `includes an authentication-cancelled-once Jane Doe option`() {
        val state =
            states.firstOrNull { it.displayName == "Jane Doe (authentication cancelled once)" }

        assertNotNull(state)
        assertEquals(MockCredentialProviderType.AUTH_CANCELLED_ONCE, state.providerType)
    }

    @Test
    fun `normal Jane Doe option uses the normal provider`() {
        val state = states.firstOrNull { it.displayName == "Jane Doe" }

        assertNotNull(state)
        assertEquals(MockCredentialProviderType.NORMAL, state.providerType)
    }
}
