package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConsentPresentationTest {

    private fun presentation(organizationName: String?) = ConsentPresentation(
        documents = emptyList(),
        organizationName = organizationName
    )

    @Test
    fun `organizationSentence is null when organisation name is null`() {
        assertNull(presentation(organizationName = null).organizationSentence)
    }

    @Test
    fun `organizationSentence is null when organisation name is blank`() {
        assertNull(presentation(organizationName = "   ").organizationSentence)
    }

    @Test
    fun `organizationSentence appends a full stop when the name has none`() {
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation(organizationName = "Yoti Ltd").organizationSentence
        )
    }

    @Test
    fun `organizationSentence keeps a single full stop when the name already ends with one`() {
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation(organizationName = "Yoti Ltd.").organizationSentence
        )
    }

    @Test
    fun `organizationSentence collapses multiple trailing full stops to one`() {
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation(organizationName = "Yoti Ltd...").organizationSentence
        )
    }

    @Test
    fun `organizationSentence trims surrounding whitespace`() {
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation(organizationName = "  Yoti Ltd  ").organizationSentence
        )
    }

    @Test
    fun `organizationSentence handles whitespace between name and trailing full stop`() {
        assertEquals(
            "The person doing the check is using an approved app powered by Yoti Ltd.",
            presentation(organizationName = "Yoti Ltd .").organizationSentence
        )
    }
}
