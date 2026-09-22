package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class AuthenticatedReaderRequestTest {

    @Test
    fun `AuthenticatedReaderRequest holds properties and supports equality`() {
        val docRequest1 = DocRequest(
            itemsRequest =
                ItemsRequest(docType = "org.iso.18013.5.1.mDL", nameSpaces = emptyMap())
        )
        val docRequest2 = DocRequest(
            itemsRequest =
                ItemsRequest(docType = "org.iso.18013.5.1.mDL", nameSpaces = emptyMap())
        )
        val uri1: Uri = mockk()
        val uri2: Uri = mockk()

        val req1 = AuthenticatedReaderRequest(docRequest = docRequest1, privacyPolicyUrl = uri1)
        val req2 = AuthenticatedReaderRequest(docRequest = docRequest2, privacyPolicyUrl = uri1)
        val req3 = AuthenticatedReaderRequest(docRequest = docRequest1, privacyPolicyUrl = uri2)

        assertEquals(docRequest1, req1.docRequest)
        assertEquals(uri1, req1.privacyPolicyUrl)
        assertEquals(req1, req2)
        assertEquals(req1.hashCode(), req2.hashCode())
        assertNotEquals(req1, req3)

        val copy = req1.copy()
        assertEquals(req1, copy)
    }
}
