package uk.gov.onelogin.sharing.holder.consent

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

class ConsentAttributeResolverTest {

    @Test
    fun `resolves known attributes to strings res IDs`() {
        val resolved = ConsentAttributeResolver.toDisplayAttributes(
            listOf(
                ConsentAttribute("family_name", false),
                ConsentAttribute("welsh_licence", false)
            )
        ).map { it.labelRes }

        assertEquals(
            listOf(
                R.string.holder_consent_attr_family_name,
                R.string.holder_consent_attr_welsh_licence
            ),
            resolved
        )
    }

    @Test
    fun `resolves any age_over_NN to the age label with its NN arg`() {
        val resolved = ConsentAttributeResolver.toDisplayAttributes(
            listOf(ConsentAttribute("age_over_21", false))
        ).single()

        assertEquals(R.string.holder_consent_attr_age_over, resolved.labelRes)
        assertEquals(21, resolved.labelArg)
    }

    @Test
    fun `applies order independent of request order`() {
        val requested = listOf(
            ConsentAttribute("birth_place", false),
            ConsentAttribute("given_name", false),
            ConsentAttribute("age_over_18", false),
            ConsentAttribute("document_number", true),
            ConsentAttribute("title", false),
            ConsentAttribute("birth_date", false),
            ConsentAttribute("family_name", false)
        )

        val ordered = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.labelRes }

        assertEquals(
            listOf(
                R.string.holder_consent_attr_document_number,
                R.string.holder_consent_attr_title,
                R.string.holder_consent_attr_family_name,
                R.string.holder_consent_attr_given_name,
                R.string.holder_consent_attr_birth_date,
                R.string.holder_consent_attr_age_over,
                R.string.holder_consent_attr_birth_place
            ),
            ordered
        )
    }

    @Test
    fun `orders age_over instances lowest to highest`() {
        val requested = listOf(
            ConsentAttribute("age_over_21", false),
            ConsentAttribute("age_over_18", false)
        )

        val ages = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.labelArg }

        assertEquals(listOf(18, 21), ages)
    }

    @Test
    fun `drops unknown attributes`() {
        val requested = listOf(
            ConsentAttribute("family_name", false),
            ConsentAttribute("eye_colour", false),
            ConsentAttribute("mystery", false)
        )

        val ordered = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.labelRes }

        assertEquals(listOf(R.string.holder_consent_attr_family_name), ordered)
    }

    @Test
    fun `preserves intentToRetain flags`() {
        val requested = listOf(
            ConsentAttribute("family_name", true),
            ConsentAttribute("given_name", false)
        )

        val resolved = ConsentAttributeResolver.toDisplayAttributes(requested)

        assertEquals(true, resolved[0].intentToRetain)
        assertEquals(false, resolved[1].intentToRetain)
    }
}
