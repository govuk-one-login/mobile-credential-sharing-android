package uk.gov.onelogin.sharing.orchestration.verificationrequest

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Test

class GbAttributeTest {

    @Test
    fun `welshLicence value is correct`() =
        assertEquals("welsh_licence", GbAttribute.WelshLicence.value)

    @Test
    fun `welshLicence validates boolean`() = assertTrue(GbAttribute.WelshLicence.validate(true))

    @Test
    fun `welshLicence rejects non-boolean`() =
        assertFalse(GbAttribute.WelshLicence.validate("true"))

    @Test
    fun `title value is correct`() = assertEquals("title", GbAttribute.Title.value)

    @Test
    fun `title validates string within limit`() = assertTrue(GbAttribute.Title.validate("Mr"))

    @Test
    fun `title rejects non-string`() = assertFalse(GbAttribute.Title.validate(123))

    @Test
    fun `provisionalDrivingPrivileges value is correct`() = assertEquals(
        "provisional_driving_privileges",
        GbAttribute.ProvisionalDrivingPrivileges.value
    )

    @Test
    fun `provisionalDrivingPrivileges validates list`() =
        assertTrue(GbAttribute.ProvisionalDrivingPrivileges.validate(listOf("B1")))

    @Test
    fun `provisionalDrivingPrivileges rejects non-list`() =
        assertFalse(GbAttribute.ProvisionalDrivingPrivileges.validate("B1"))

    @Test
    fun `NAMESPACE is correct`() = assertEquals("org.iso.18013.5.1.GB", GbAttribute.NAMESPACE)
}
