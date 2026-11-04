package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute.Companion.toConsentAttributes
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.Companion.withIntentToRetain
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.Companion.withoutIntentToRetain

object ConsentPresentationTestData {

    val fullyRetainedAttributes = listOf(
        MdlAttribute.Portrait,
        MdlAttribute.GivenName,
        MdlAttribute.FamilyName,
        MdlAttribute.EyeColour
    ).withIntentToRetain()

    val noRetainedAttributes = listOf(
        MdlAttribute.AgeOver(18)
    ).withoutIntentToRetain()

    val partiallyRetainedAttributes = fullyRetainedAttributes + noRetainedAttributes

    fun createConsentNamespaces(elements: Map<MdlAttribute, Boolean>) = listOf(
        ConsentNamespace(
            nameSpace = "org.iso.18013.5.1",
            attributes = elements.toConsentAttributes()
        )
    )

    fun createConsentDocumentSingleton(
        docType: String = "org.iso.18013.5.1.mDL",
        namespaces: List<ConsentNamespace> = createConsentNamespaces(
            partiallyRetainedAttributes
        )
    ) = listOf(
        ConsentDocument(
            docType = docType,
            namespaces = namespaces
        )
    )

    fun createConsentPresentation(
        documents: List<ConsentDocument> = createConsentDocumentSingleton(),
        privacyPolicyUrl: String? = "https://www.this.is.stub.data.com",
        organizationName: String? = "Stub-DVS Ltd.",
        isBusy: Boolean = false
    ) = ConsentPresentation(
        documents = documents,
        privacyPolicyUrl = privacyPolicyUrl,
        organizationName = organizationName,
        isBusy = isBusy
    )
}
