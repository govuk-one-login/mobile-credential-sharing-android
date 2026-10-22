package uk.gov.onelogin.sharing.holder.consent

import kotlin.test.assertEquals
import org.junit.Test

class ConsentCardTest {

    private fun attr(labelRes: Int, intentToRetain: Boolean, labelArg: Int? = null) =
        DisplayAttribute(labelRes = labelRes, labelArg = labelArg, intentToRetain = intentToRetain)

    @Test
    fun `derives NO_DETAILS_KEPT when no attribute is retained`() {
        val attributes = listOf(attr(1, false), attr(2, false))

        val card = ConsentCard.from(attributes)

        assertEquals(RetentionVariant.NO_DETAILS_KEPT, card.retentionVariant)
    }

    @Test
    fun `derives ALL_DETAILS_KEPT when every attribute is retained`() {
        val attributes = listOf(attr(1, true), attr(2, true))

        val card = ConsentCard.from(attributes)

        assertEquals(RetentionVariant.ALL_DETAILS_KEPT, card.retentionVariant)
    }

    @Test
    fun `derives SOME_DETAILS_KEPT for a mix of retained and non-retained`() {
        val attributes = listOf(attr(1, true), attr(2, false))

        val card = ConsentCard.from(attributes)

        assertEquals(RetentionVariant.SOME_DETAILS_KEPT, card.retentionVariant)
    }

    @Test
    fun `treats an empty list defensively as NO_DETAILS_KEPT`() {
        val card = ConsentCard.from(emptyList())

        assertEquals(RetentionVariant.NO_DETAILS_KEPT, card.retentionVariant)
        assertEquals(0, card.attributeCount)
    }

    @Test
    fun `reports the total attribute count`() {
        val attributes = listOf(attr(1, true), attr(2, false), attr(3, false))

        val card = ConsentCard.from(attributes)

        assertEquals(3, card.attributeCount)
    }

    @Test
    fun `partitions kept and not-kept while preserving order`() {
        val keptFirst = attr(1, true)
        val notKeptFirst = attr(2, false)
        val keptSecond = attr(3, true)
        val notKeptSecond = attr(4, false)
        val attributes = listOf(keptFirst, notKeptFirst, keptSecond, notKeptSecond)

        val card = ConsentCard.from(attributes)

        assertEquals(listOf(keptFirst, keptSecond), card.keptAttributes)
        assertEquals(listOf(notKeptFirst, notKeptSecond), card.notKeptAttributes)
    }

    @Test
    fun `keeps the full ordered attribute list unchanged`() {
        val attributes = listOf(attr(1, true), attr(2, false), attr(3, true))

        val card = ConsentCard.from(attributes)

        assertEquals(attributes, card.attributes)
    }
}
