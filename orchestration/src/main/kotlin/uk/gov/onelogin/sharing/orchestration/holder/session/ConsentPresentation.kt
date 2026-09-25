package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * The information shown to the User on the "Agree to Share" consent screen.
 *
 * Describes exactly what will be included in the DeviceResponse after filtering process
 * plus the verified ReaderAuth privacy-policy link and organisation name.
 *
 * @param documents The filtered attributes grouped by document type and namespace.
 * @param privacyPolicyUrl The verified ReaderAuth privacy-policy URI
 * @param organizationName The verified ReaderAuth organisation name
 */
data class ConsentPresentation(
    val documents: List<ConsentDocument>,
    val privacyPolicyUrl: String? = null,
    val organizationName: String? = null
) {
    /**
     * The organisation-name sentence shown to the User, with punctuation normalised so it always
     * ends in exactly one full stop. Returns `null` when no [organizationName] is available.
     */
    val organizationSentence: String?
        get() = organizationName
            ?.let(::normaliseOrganizationName)
            ?.let { name -> ORGANIZATION_SENTENCE_PREFIX + name }

    private fun normaliseOrganizationName(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        return trimmed.trimEnd('.').trimEnd() + "."
    }

    companion object {
        /**
         * Prefix for the organisation-name sentence. The organisation name (with a single trailing
         * full stop) is appended to this string.
         */
        const val ORGANIZATION_SENTENCE_PREFIX =
            "The person doing the check is using an approved app powered by "
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
