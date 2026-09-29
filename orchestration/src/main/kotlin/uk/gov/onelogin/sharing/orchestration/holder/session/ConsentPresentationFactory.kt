package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.MatchedAttribute
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

/**
 * Builds the [ConsentPresentation] shown on the "Agree to Share" screen.
 *
 * The displayed attributes are driven by the output of filtering the credential against
 * the selected, authenticated request. Requested attributes that do not have any matching
 * attributes on the credential are not presented.
 */
object ConsentPresentationFactory {

    fun create(
        matchedAttributes: Map<String, List<MatchedAttribute>>,
        authenticatedReaderRequest: AuthenticatedReaderRequest?
    ): ConsentPresentation {
        val docType = authenticatedReaderRequest?.docRequest?.itemsRequest?.docType

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
