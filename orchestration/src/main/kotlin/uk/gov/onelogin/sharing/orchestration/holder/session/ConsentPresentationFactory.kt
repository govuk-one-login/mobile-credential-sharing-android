package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.MatchedAttribute
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

/**
 * Builds the [ConsentPresentation] shown on the "Agree to Share" screen.
 *
 * The displayed attributes are driven entirely by the output of filtering the
 * credential against the DeviceRequest. Used what will be included in the DeviceResponse
 */
object ConsentPresentationFactory {

    fun create(
        deviceRequest: DeviceRequest,
        matchedAttributes: Map<String, List<MatchedAttribute>>,
        authenticatedReaderRequest: AuthenticatedReaderRequest?
    ): ConsentPresentation {
        val docType = deviceRequest.docRequests.firstOrNull()?.itemsRequest?.docType

        val namespaces = matchedAttributes
            .filterValues { it.isNotEmpty() }
            .map { (nameSpace, attributes) ->
                ConsentNamespace(
                    nameSpace = nameSpace,
                    attributes = attributes.map {
                        ConsentAttribute(
                            elementIdentifier = it.elementIdentifier,
                            intentToRetain = it.intentToRetain
                        )
                    }
                )
            }

        val documents = if (namespaces.isEmpty() || docType == null) {
            emptyList()
        } else {
            listOf(ConsentDocument(docType = docType, namespaces = namespaces))
        }

        return ConsentPresentation(
            documents = documents,
            privacyPolicyUrl = authenticatedReaderRequest?.privacyPolicyUrl?.toString(),
            organizationName = authenticatedReaderRequest?.readerOrganizationName
        )
    }
}
