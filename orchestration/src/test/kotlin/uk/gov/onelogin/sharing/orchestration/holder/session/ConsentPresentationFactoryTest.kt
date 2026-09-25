package uk.gov.onelogin.sharing.orchestration.holder.session

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.MatchedAttribute
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

class ConsentPresentationFactoryTest {

    private val docType = "org.iso.18013.5.1.mDL"
    private val namespace = "org.iso.18013.5.1"

    private fun deviceRequest(nameSpaces: Map<String, Map<String, Boolean>>) = DeviceRequest(
        version = "1.0",
        docRequests = listOf(DocRequest(ItemsRequest(docType = docType, nameSpaces = nameSpaces)))
    )

    @Test
    fun `maps retained attributes onto the display model`() {
        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(
                mapOf(namespace to mapOf("family_name" to true, "given_name" to false))
            ),
            matchedAttributes = mapOf(
                namespace to listOf(
                    MatchedAttribute("family_name", true),
                    MatchedAttribute("given_name", false)
                )
            ),
            authenticatedReaderRequest = null
        )

        assertEquals(1, presentation.documents.size)
        val document = presentation.documents.single()
        assertEquals(docType, document.docType)
        assertEquals(
            listOf(
                ConsentAttribute("family_name", true),
                ConsentAttribute("given_name", false)
            ),
            document.namespaces.single().attributes
        )
    }

    @Test
    fun `only retained attributes are displayed`() {
        // Request asks for three, but only one was retained by filtering.
        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(
                mapOf(
                    namespace to mapOf(
                        "family_name" to true,
                        "given_name" to true,
                        "portrait" to false
                    )
                )
            ),
            matchedAttributes = mapOf(
                namespace to listOf(MatchedAttribute("family_name", true))
            ),
            authenticatedReaderRequest = null
        )

        assertEquals(
            listOf(ConsentAttribute("family_name", true)),
            presentation.documents.single().namespaces.single().attributes
        )
    }

    @Test
    fun `resolved age attribute is displayed with inherited intent to retain`() {
        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(mapOf(namespace to mapOf("age_over_18" to true))),
            matchedAttributes = mapOf(
                namespace to listOf(MatchedAttribute("age_over_21", true))
            ),
            authenticatedReaderRequest = null
        )

        assertEquals(
            listOf(ConsentAttribute("age_over_21", true)),
            presentation.documents.single().namespaces.single().attributes
        )
    }

    @Test
    fun `namespaces with no retained attributes are omitted`() {
        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(mapOf(namespace to mapOf("family_name" to true))),
            matchedAttributes = emptyMap(),
            authenticatedReaderRequest = null
        )

        assertEquals(emptyList(), presentation.documents)
    }

    @Test
    fun `privacy policy and organisation name come from the reader request`() {
        val uri = mockk<Uri>()
        every { uri.toString() } returns "https://verifier.example/privacy"

        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(mapOf(namespace to mapOf("family_name" to true))),
            matchedAttributes = mapOf(
                namespace to listOf(MatchedAttribute("family_name", true))
            ),
            authenticatedReaderRequest = AuthenticatedReaderRequest(
                docRequest = mockk(relaxed = true),
                privacyPolicyUrl = uri,
                readerOrganizationName = "Yoti Ltd"
            )
        )

        assertEquals("https://verifier.example/privacy", presentation.privacyPolicyUrl)
        assertEquals("Yoti Ltd", presentation.organizationName)
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation.organizationSentence
        )
    }

    @Test
    fun `privacy policy and organisation name are null when reader request is null`() {
        val presentation = ConsentPresentationFactory.create(
            deviceRequest = deviceRequest(mapOf(namespace to mapOf("family_name" to true))),
            matchedAttributes = mapOf(
                namespace to listOf(MatchedAttribute("family_name", true))
            ),
            authenticatedReaderRequest = null
        )

        assertNull(presentation.privacyPolicyUrl)
        assertNull(presentation.organizationName)
        assertNull(presentation.organizationSentence)
    }
}
