package uk.gov.onelogin.sharing.core.implementation

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailAssertions.hasDescription
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailAssertions.hasTicket
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailData.detailWithDescription
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailData.detailWithTicket
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailData.implementationDetail
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetailData.implementationDetailCopy

class ImplementationDetailTest {

    @Test
    fun equalityCheck() {
        assertEquals(implementationDetail, implementationDetailCopy)
        assertNotEquals(implementationDetail, detailWithTicket)
        assertNotEquals(implementationDetail, detailWithDescription)
    }

    @Test
    fun assertAgainstTicket() {
        assertThat(
            detailWithTicket,
            hasTicket("DCMAW--1")
        )
    }

    @Test
    fun assertAgainstDescription() {
        assertThat(
            detailWithDescription,
            hasDescription("A unit test")
        )
    }
}
