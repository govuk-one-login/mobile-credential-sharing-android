package uk.gov.onelogin.sharing.holder.consent

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

class ConsentAttributeResolverTest {

    private val baseNameSpace = "org.iso.18013.5.1"
    private val gbNameSpace = "org.iso.18013.5.1.GB"

    private fun DisplayAttribute.labelRes(): Int = (label as DisplayAttribute.Label.Resource).res

    private fun DisplayAttribute.labelArg(): Int? = (label as DisplayAttribute.Label.Resource).arg

    @Test
    fun `resolves known attributes to strings res IDs`() {
        val resolved = ConsentAttributeResolver.toDisplayAttributes(
            listOf(
                ConsentAttribute("family_name", false, nameSpace = baseNameSpace),
                ConsentAttribute("welsh_licence", false, nameSpace = gbNameSpace)
            )
        ).map { it.labelRes() }

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
            listOf(ConsentAttribute("age_over_21", false, nameSpace = baseNameSpace))
        ).single()

        assertEquals(R.string.holder_consent_attr_age_over, resolved.labelRes())
        assertEquals(21, resolved.labelArg())
    }

    @Test
    fun `applies order independent of request order`() {
        val requested = listOf(
            ConsentAttribute("birth_place", false, nameSpace = baseNameSpace),
            ConsentAttribute("given_name", false, nameSpace = baseNameSpace),
            ConsentAttribute("age_over_18", false, nameSpace = baseNameSpace),
            ConsentAttribute("document_number", true, nameSpace = baseNameSpace),
            ConsentAttribute("title", false, nameSpace = gbNameSpace),
            ConsentAttribute("birth_date", false, nameSpace = baseNameSpace),
            ConsentAttribute("family_name", false, nameSpace = baseNameSpace)
        )

        val ordered = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.labelRes() }

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
            ConsentAttribute("age_over_21", false, nameSpace = baseNameSpace),
            ConsentAttribute("age_over_18", false, nameSpace = baseNameSpace)
        )

        val ages = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.labelArg() }

        assertEquals(listOf(18, 21), ages)
    }

    @Test
    fun `renders unmapped attributes as a raw namespace - identifier label`() {
        val requested = listOf(
            ConsentAttribute("family_name", false, nameSpace = baseNameSpace),
            ConsentAttribute("eye_colour", false, nameSpace = baseNameSpace),
            ConsentAttribute("un_mapped", false, nameSpace = "org.iso.18013.5.1.XX")
        )

        val labels = ConsentAttributeResolver.toDisplayAttributes(requested).map { it.label }

        assertEquals(
            listOf(
                DisplayAttribute.Label.Resource(R.string.holder_consent_attr_family_name),
                DisplayAttribute.Label.Raw("org.iso.18013.5.1 - eye_colour"),
                DisplayAttribute.Label.Raw("org.iso.18013.5.1.XX - un_mapped")
            ),
            labels
        )
    }

    @Test
    fun `renders a known identifier in an unexpected namespace as raw`() {
        // family_name is only mapped under org.iso.18013.5.1; a different namespace must not match.
        val requested = listOf(
            ConsentAttribute("family_name", false, nameSpace = gbNameSpace)
        )

        val label = ConsentAttributeResolver.toDisplayAttributes(requested).single().label

        assertEquals(
            DisplayAttribute.Label.Raw("org.iso.18013.5.1.GB - family_name"),
            label
        )
    }

    @Test
    fun `preserves intentToRetain flags`() {
        val requested = listOf(
            ConsentAttribute("family_name", true, nameSpace = baseNameSpace),
            ConsentAttribute("given_name", false, nameSpace = baseNameSpace)
        )

        val resolved = ConsentAttributeResolver.toDisplayAttributes(requested)

        assertEquals(true, resolved[0].intentToRetain)
        assertEquals(false, resolved[1].intentToRetain)
    }
}
