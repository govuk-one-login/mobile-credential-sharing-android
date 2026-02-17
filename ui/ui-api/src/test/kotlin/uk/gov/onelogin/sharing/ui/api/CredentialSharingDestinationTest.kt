package uk.gov.onelogin.sharing.ui.api

import org.junit.Assert.assertNotNull
import org.junit.Test

class CredentialSharingDestinationTest {

    @Test
    fun `destinations are reachable`() {
        assertNotNull(CredentialSharingDestination.Holder)
        assertNotNull(CredentialSharingDestination.Verifier)
    }
}
