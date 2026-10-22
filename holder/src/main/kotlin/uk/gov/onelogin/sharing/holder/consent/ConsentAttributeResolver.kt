package uk.gov.onelogin.sharing.holder.consent

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

/**
 * Maps DataElementIdentifier values to labels and the display order.
 */
object ConsentAttributeResolver {

    private const val AGE_OVER_PREFIX = "age_over_"

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
    private fun attributeLabelRes(elementIdentifier: String): Int? {
        if (elementIdentifier.startsWith(AGE_OVER_PREFIX)) {
            return R.string.holder_consent_attr_age_over
        }
        return labelResByIdentifier[elementIdentifier]
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

    /** Gets strings and orders attributes into the display order.*/
    fun toDisplayAttributes(attributes: List<ConsentAttribute>): List<DisplayAttribute> = attributes
        .mapNotNull { attribute ->
            val labelRes = attributeLabelRes(attribute.elementIdentifier)
                ?: return@mapNotNull null
            orderIndexOf(attribute.elementIdentifier) to DisplayAttribute(
                labelRes = labelRes,
                labelArg = ageOverValue(attribute.elementIdentifier),
                intentToRetain = attribute.intentToRetain
            )
        }
        .sortedWith(compareBy({ it.first }, { it.second.labelArg ?: Int.MIN_VALUE }))
        .map { it.second }

    private val labelResByIdentifier: Map<String, Int> = mapOf(
        "document_number" to R.string.holder_consent_attr_document_number,
        "portrait" to R.string.holder_consent_attr_portrait,
        "title" to R.string.holder_consent_attr_title,
        "family_name" to R.string.holder_consent_attr_family_name,
        "given_name" to R.string.holder_consent_attr_given_name,
        "expiry_date" to R.string.holder_consent_attr_expiry_date,
        "birth_date" to R.string.holder_consent_attr_birth_date,
        "birth_place" to R.string.holder_consent_attr_birth_place,
        "resident_address" to R.string.holder_consent_attr_resident_address,
        "resident_city" to R.string.holder_consent_attr_resident_city,
        "resident_postal_code" to R.string.holder_consent_attr_resident_postal_code,
        "issue_date" to R.string.holder_consent_attr_issue_date,
        "issuing_authority" to R.string.holder_consent_attr_issuing_authority,
        "issuing_country" to R.string.holder_consent_attr_issuing_country,
        "un_distinguishing_sign" to R.string.holder_consent_attr_un_distinguishing_sign,
        "driving_privileges" to R.string.holder_consent_attr_driving_privileges,
        "provisional_driving_privileges" to
            R.string.holder_consent_attr_provisional_driving_privileges,
        "welsh_licence" to R.string.holder_consent_attr_welsh_licence
    )
}

/**
 * A consent attribute resolved to its label, ready to render on the Agree to Share card.
 *
 * @param labelArg The NN value for an `age_over_NN` label. `null` for labels that take no argument.
 */
data class DisplayAttribute(
    @param:StringRes val labelRes: Int,
    val labelArg: Int? = null,
    val intentToRetain: Boolean
)
