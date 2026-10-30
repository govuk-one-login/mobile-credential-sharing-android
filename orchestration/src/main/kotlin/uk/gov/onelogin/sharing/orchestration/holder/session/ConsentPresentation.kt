package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * The information shown to the User on the consent screen.
 *
 * Describes exactly what will be included in the DeviceResponse after filtering process
 * plus the verified ReaderAuth privacy-policy link and organisation name.
 *
 * @param documents The filtered attributes grouped by document type and namespace.
 * @param privacyPolicyUrl The verified ReaderAuth privacy-policy URI.
 * @param organizationName The verified ReaderAuth organisation name.
 */
data class ConsentPresentation(
    val documents: List<ConsentDocument>,
    val organizationName: String,
    val privacyPolicyUrl: String,
    val isBusy: Boolean = false
) {
    /**
     * The organisation name with punctuation normalised so that, once placed into a sentence, it
     * ends in exactly one full stop.
     */
    val normalisedOrganizationName: String
        get() = organizationName.trim().trimEnd('.').trimEnd() + "."
}

/**
 * A single requested document type and its filtered namespaces.
 */
data class ConsentDocument(val docType: String, val namespaces: List<ConsentNamespace>)

/**
 * A single namespace and the filtered attributes retained within it.
 */
data class ConsentNamespace(val nameSpace: String, val attributes: List<ConsentAttribute>)

/**
 * A single attribute that will be shared with the Verifier.
 *
 * @param elementIdentifier The credential element identifier that will be included in the
 * DeviceResponse (for example `family_name`, or a resolved `age_over_18`).
 * @param intentToRetain Whether the Verifier declared an intent to retain this attribute.
 * @param nameSpace The namespace the attribute belongs to (for example `org.iso.18013.5.1` or the
 * custom `org.iso.18013.5.1.GB`). Used to resolve the display label.
 */
data class ConsentAttribute(
    val elementIdentifier: String,
    val intentToRetain: Boolean,
    val nameSpace: String
)
