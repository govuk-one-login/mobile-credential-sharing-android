package uk.gov.onelogin.sharing.holder.consent

import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

/**
 * Shared sample data and builder for [AgreeToShareCard] tests.
 */
object AgreeToShareCardSamples {

    const val ORGANIZATION_NAME = "Example Organisation."

    val noDetailsKept: List<Pair<String, Boolean>> = listOf(
        "family_name" to false,
        "given_name" to false,
        "portrait" to false
    )

    val allDetailsKept: List<Pair<String, Boolean>> = listOf(
        "family_name" to true,
        "given_name" to true,
        "document_number" to true
    )

    val someDetailsKept: List<Pair<String, Boolean>> = listOf(
        "document_number" to true,
        "family_name" to true,
        "portrait" to false,
        "age_over_18" to false
    )

    fun card(vararg attributes: Pair<String, Boolean>): ConsentCard = card(attributes.toList())

    fun card(attributes: List<Pair<String, Boolean>>): ConsentCard = ConsentCard.from(
        ConsentAttributeResolver.toDisplayAttributes(
            attributes.map { (id, retain) -> ConsentAttribute(id, retain) }
        )
    )
}
