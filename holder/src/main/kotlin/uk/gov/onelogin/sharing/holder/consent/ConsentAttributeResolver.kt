package uk.gov.onelogin.sharing.holder.consent

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

/**
 * Maps (namespace, DataElementIdentifier) values to labels and the display order.
 */
object ConsentAttributeResolver {

    private const val AGE_OVER_PREFIX = "age_over_"

    private const val ISO_MDL_NAMESPACE = "org.iso.18013.5.1"
    private const val ISO_MDL_GB_NAMESPACE = "org.iso.18013.5.1.GB"

    private data class AttributeKey(val nameSpace: String, val elementIdentifier: String)

    private val displayOrder: List<String> = listOf(
        "document_number",
        "portrait",
        "title",
        "family_name",
        "given_name",
        "expiry_date",
        "birth_date",
        AGE_OVER_PREFIX,
        "birth_place",
        "resident_address",
        "resident_city",
        "resident_postal_code",
        "issue_date",
        "issuing_authority",
        "issuing_country",
        "un_distinguishing_sign",
        "driving_privileges",
        "provisional_driving_privileges",
        "welsh_licence"
    )

    private val orderIndex: Map<String, Int> =
        displayOrder.withIndex().associate { (index, key) -> key to index }

    @StringRes
    private fun attributeLabelRes(nameSpace: String, elementIdentifier: String): Int? {
        if (elementIdentifier.startsWith(AGE_OVER_PREFIX)) {
            return R.string.holder_consent_attr_age_over
        }
        return labelResByAttribute[AttributeKey(nameSpace, elementIdentifier)]
    }

    /** The NN from an `age_over_NN` identifier, or `null` for other identifiers. */
    private fun ageOverValue(elementIdentifier: String): Int? =
        if (elementIdentifier.startsWith(AGE_OVER_PREFIX)) {
            elementIdentifier.removePrefix(AGE_OVER_PREFIX).toIntOrNull()
        } else {
            null
        }

    /** Sort index for an identifier; unknown identifiers sort to the end. */
    private fun orderIndexOf(elementIdentifier: String): Int {
        val key = if (elementIdentifier.startsWith(AGE_OVER_PREFIX)) {
            AGE_OVER_PREFIX
        } else {
            elementIdentifier
        }
        return orderIndex[key] ?: Int.MAX_VALUE
    }

    /**
     * Resolves attributes to display labels in display order. Attributes with a known
     * (namespace, identifier) mapping use their GDS label; unmapped attributes fall back to a raw
     * `namespace - identifier` label so they still appear on the card and stand out as unexpected.
     */
    fun toDisplayAttributes(attributes: List<ConsentAttribute>): List<DisplayAttribute> = attributes
        .map { attribute ->
            val labelRes = attributeLabelRes(attribute.nameSpace, attribute.elementIdentifier)
            val label = if (labelRes != null) {
                DisplayAttribute.Label.Resource(
                    res = labelRes,
                    arg = ageOverValue(attribute.elementIdentifier)
                )
            } else {
                DisplayAttribute.Label.Raw(
                    "${attribute.nameSpace} - ${attribute.elementIdentifier}"
                )
            }
            orderIndexOf(attribute.elementIdentifier) to DisplayAttribute(
                label = label,
                intentToRetain = attribute.intentToRetain
            )
        }
        .sortedWith(compareBy({ it.first }, { (it.second.label.argOrNull()) ?: Int.MIN_VALUE }))
        .map { it.second }

    private fun DisplayAttribute.Label.argOrNull(): Int? =
        (this as? DisplayAttribute.Label.Resource)?.arg

    private val labelResByAttribute: Map<AttributeKey, Int> = mapOf(
        AttributeKey(ISO_MDL_NAMESPACE, "document_number") to
            R.string.holder_consent_attr_document_number,
        AttributeKey(ISO_MDL_NAMESPACE, "portrait") to R.string.holder_consent_attr_portrait,
        AttributeKey(ISO_MDL_GB_NAMESPACE, "title") to R.string.holder_consent_attr_title,
        AttributeKey(ISO_MDL_NAMESPACE, "family_name") to R.string.holder_consent_attr_family_name,
        AttributeKey(ISO_MDL_NAMESPACE, "given_name") to R.string.holder_consent_attr_given_name,
        AttributeKey(ISO_MDL_NAMESPACE, "expiry_date") to R.string.holder_consent_attr_expiry_date,
        AttributeKey(ISO_MDL_NAMESPACE, "birth_date") to R.string.holder_consent_attr_birth_date,
        AttributeKey(ISO_MDL_NAMESPACE, "birth_place") to R.string.holder_consent_attr_birth_place,
        AttributeKey(ISO_MDL_NAMESPACE, "resident_address") to
            R.string.holder_consent_attr_resident_address,
        AttributeKey(ISO_MDL_NAMESPACE, "resident_city") to
            R.string.holder_consent_attr_resident_city,
        AttributeKey(ISO_MDL_NAMESPACE, "resident_postal_code") to
            R.string.holder_consent_attr_resident_postal_code,
        AttributeKey(ISO_MDL_NAMESPACE, "issue_date") to R.string.holder_consent_attr_issue_date,
        AttributeKey(ISO_MDL_NAMESPACE, "issuing_authority") to
            R.string.holder_consent_attr_issuing_authority,
        AttributeKey(ISO_MDL_NAMESPACE, "issuing_country") to
            R.string.holder_consent_attr_issuing_country,
        AttributeKey(ISO_MDL_NAMESPACE, "un_distinguishing_sign") to
            R.string.holder_consent_attr_un_distinguishing_sign,
        AttributeKey(ISO_MDL_NAMESPACE, "driving_privileges") to
            R.string.holder_consent_attr_driving_privileges,
        AttributeKey(ISO_MDL_GB_NAMESPACE, "provisional_driving_privileges") to
            R.string.holder_consent_attr_provisional_driving_privileges,
        AttributeKey(ISO_MDL_GB_NAMESPACE, "welsh_licence") to
            R.string.holder_consent_attr_welsh_licence
    )
}

/**
 * A consent attribute resolved to its label, ready to render on the Agree to Share card.
 */
data class DisplayAttribute(val label: Label, val intentToRetain: Boolean) {
    /**
     * The label to render for an attribute.
     *
     * - [Resource]: a known attribute mapped to a GDS string resource.
     * - [Raw]: an unmapped attribute shown verbatim as `namespace - identifier`.
     */
    sealed interface Label {
        /** @param arg The NN value for an `age_over_NN` label; `null` for labels with no argument. */
        data class Resource(@param:StringRes val res: Int, val arg: Int? = null) : Label

        data class Raw(val text: String) : Label
    }
}
