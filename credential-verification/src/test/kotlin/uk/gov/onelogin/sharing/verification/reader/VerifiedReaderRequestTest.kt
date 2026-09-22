package uk.gov.onelogin.sharing.verification.reader

import io.mockk.mockk
import java.security.cert.X509Certificate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class VerifiedReaderRequestTest {

    @Test
    fun `VerifiedReaderRequest holds properties and supports equality`() {
        val docRequest1 = DocRequest(
            itemsRequest = ItemsRequest(
                docType = "org.iso.18013.5.1.mDL",
                nameSpaces = emptyMap()
            )
        )
        val docRequest2 = DocRequest(
            itemsRequest = ItemsRequest(
                docType = "org.iso.18013.5.1.mDL",
                nameSpaces = emptyMap()
            )
        )
        val cert1: X509Certificate = mockk()
        val cert2: X509Certificate = mockk()

        val req1 = VerifiedReaderRequest(docRequest = docRequest1, readerCertificate = cert1)
        val req2 = VerifiedReaderRequest(docRequest = docRequest2, readerCertificate = cert1)
        val req3 = VerifiedReaderRequest(docRequest = docRequest1, readerCertificate = cert2)

        assertEquals(docRequest1, req1.docRequest)
        assertEquals(cert1, req1.readerCertificate)
        assertEquals(req1, req2)
        assertEquals(req1.hashCode(), req2.hashCode())
        assertNotEquals(req1, req3)

        val copy = req1.copy()
        assertEquals(req1, copy)
    }
}
