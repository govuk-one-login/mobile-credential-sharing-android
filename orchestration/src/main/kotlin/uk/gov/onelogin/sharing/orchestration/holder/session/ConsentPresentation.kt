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
    val privacyPolicyUrl: String? = null,
    val organizationName: String? = null
) {
    /**
     * The organisation name with punctuation normalised so that, once placed into a sentence, it
     * ends in exactly one full stop.
     *
     * Returns `null` when no [organizationName] is available.
     */
    val normalisedOrganizationName: String?
        get() {
            val trimmed = organizationName?.trim().orEmpty()

            return if (trimmed.isEmpty()) {
                null
            } else {
                trimmed.trimEnd('.').trimEnd() + "."
            }
        }
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
 */
data class ConsentAttribute(val elementIdentifier: String, val intentToRetain: Boolean)
