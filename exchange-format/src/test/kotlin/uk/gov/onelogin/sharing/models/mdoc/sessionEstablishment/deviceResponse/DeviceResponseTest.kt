package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlin.test.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

class DeviceResponseTest {

    private var version = "1.0"
    private var documents: List<VerifiableDocument.WithPresentation>? = null
    private var errors: Map<String, Status>? = null
    private var status: Status = Status.OK

    private val response by lazy {
        DeviceResponse(
            version = version,
            documents = documents,
            documentErrors = errors,
            status = status
        )
    }

    @Test
    fun `Iterator defers to provided documents when available`() {
        val document: VerifiableDocument.WithPresentation =
            SharingVerifiableDocumentWithPresentation(
                SharingVerifiableDocument(
                    docType = "Unit test",
                    issuerSigned = SharingIssuerSigned(
                        emptyMap(),
                        byteArrayOf()
                    )
                ),
                SharingDeviceSigned(
                    byteArrayOf(),
                    byteArrayOf()
                )
            )
        documents = listOf(document)

        assertThat(
            response,
            contains(document)
        )
    }
}
