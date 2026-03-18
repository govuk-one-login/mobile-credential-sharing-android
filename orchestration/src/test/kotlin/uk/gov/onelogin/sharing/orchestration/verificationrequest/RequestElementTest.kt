package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Test

class RequestElementTest {

    @Test
    fun `GivenName value is given_name`() {
        assertEquals("given_name", RequestElement.GivenName.value)
    }

    @Test
    fun `FamilyName value is family_name`() {
        assertEquals("family_name", RequestElement.FamilyName.value)
    }

    @Test
    fun `Portrait value is portrait`() {
        assertEquals("portrait", RequestElement.Portrait.value)
    }

    @Test
    fun `AgeOver formats value with age`() {
        assertEquals("age_over_18", RequestElement.AgeOver(18).value)
    }

    @Test
    fun `AgeOver formats value with different age`() {
        assertEquals("age_over_21", RequestElement.AgeOver(21).value)
    }

    @Test
    fun `AgeOver with zero`() {
        assertEquals("age_over_0", RequestElement.AgeOver(0).value)
    }

    @Test
    fun `AgeOver with negative age`() {
        assertEquals("age_over_-1", RequestElement.AgeOver(-1).value)
    }

    @Test
    fun `Custom returns provided value`() {
        assertEquals("custom_field", RequestElement.Custom("custom_field").value)
    }

    @Test
    fun `Custom with empty string`() {
        assertEquals("", RequestElement.Custom("").value)
    }

    @Test
    fun `Custom with special characters`() {
        assertEquals(
            "field.with/special-chars",
            RequestElement.Custom("field.with/special-chars").value
        )
    }
}
