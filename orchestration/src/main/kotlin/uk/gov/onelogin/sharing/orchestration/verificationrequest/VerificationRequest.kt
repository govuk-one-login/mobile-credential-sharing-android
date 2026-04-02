package uk.gov.onelogin.sharing.orchestration.verificationrequest

data class VerificationRequest(val documentType: String, val attributeGroup: AttributeGroup) {
    val requestedElements: List<String>
        get() = attributeGroup.attributes.keys.map { it.value }

    companion object {
        fun typed(documentType: DocumentType, attributeGroup: AttributeGroup): VerificationRequest =
            VerificationRequest(
                documentType = documentType.value,
                attributeGroup = attributeGroup
            )

        fun raw(
            documentType: String,
            requestedElements: Map<String, Boolean>
        ): VerificationRequest = VerificationRequest(
            documentType = documentType,
            attributeGroup = AttributeGroup(
                requestedElements.map { (key, retain) ->
                    MdlAttribute.Custom(key) to retain
                }.toMap()
            )
        )
    }
}
