package uk.gov.onelogin.sharing.holder.consent

/**
 * Which retention variant of the Agree to Share card to show derived from the attributes'
 * intentToRetain flags.
 *
 * - [NO_DETAILS_KEPT]: all attribute have `intentToRetain = false`.
 * - [ALL_DETAILS_KEPT]: all attributes have `intentToRetain = true`.
 * - [SOME_DETAILS_KEPT]: a mix of retained and non-retained attributes.
 */
enum class RetentionVariant { NO_DETAILS_KEPT, ALL_DETAILS_KEPT, SOME_DETAILS_KEPT }

/**
 * The fully resolved, display-ready model for a single document's 'Agree to Share' card.
 *
 * @param attributeCount The number of attributes shown, used in the card heading.
 * @param retentionVariant Which retention variant to render.
 * @param attributes All resolved attributes in display order (used by the all/none-kept variants).
 * @param keptAttributes The attributes the Verifier intends to retain, in display order.
 * @param notKeptAttributes The attributes the Verifier will not retain, in display order.
 */
data class ConsentCard(
    val attributeCount: Int,
    val retentionVariant: RetentionVariant,
    val attributes: List<DisplayAttribute>,
    val keptAttributes: List<DisplayAttribute>,
    val notKeptAttributes: List<DisplayAttribute>
) {
    companion object {
        fun from(attributes: List<DisplayAttribute>): ConsentCard {
            val kept = attributes.filter { it.intentToRetain }
            val notKept = attributes.filterNot { it.intentToRetain }
            val variant = when {
                kept.isEmpty() -> RetentionVariant.NO_DETAILS_KEPT
                notKept.isEmpty() -> RetentionVariant.ALL_DETAILS_KEPT
                else -> RetentionVariant.SOME_DETAILS_KEPT
            }
            return ConsentCard(
                attributeCount = attributes.size,
                retentionVariant = variant,
                attributes = attributes,
                keptAttributes = kept,
                notKeptAttributes = notKept
            )
        }
    }
}
