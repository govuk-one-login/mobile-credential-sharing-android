package uk.gov.onelogin.sharing.orchestration.verificationrequest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Enclosed::class)
class MdlAttributeTest {

    @RunWith(Parameterized::class)
    class TextElement150CharTests(
        private val element: MdlAttribute,
        private val expectedValue: String
    ) {
        @Test
        fun `value matches expected identifier`() = assertEquals(expectedValue, element.value)

        @Test
        fun `validates string within limit`() = assertTrue(element.validate("valid"))

        @Test
        fun `validates string at 150 char limit`() = assertTrue(element.validate("a".repeat(150)))

        @Test
        fun `rejects string over 150 chars`() = assertFalse(element.validate("a".repeat(151)))

        @Test
        fun `rejects non-string type`() = assertFalse(element.validate(123))

        companion object {
            @JvmStatic
            @Parameters(name = "{1}")
            fun data() = listOf(
                arrayOf(MdlAttribute.FamilyName, "family_name"),
                arrayOf(MdlAttribute.GivenName, "given_name"),
                arrayOf(MdlAttribute.IssuingAuthority, "issuing_authority"),
                arrayOf(MdlAttribute.DocumentNumber, "document_number"),
                arrayOf(MdlAttribute.BirthPlace, "birth_place"),
                arrayOf(MdlAttribute.ResidentAddress, "resident_address"),
                arrayOf(MdlAttribute.ResidentPostalCode, "resident_postal_code"),
                arrayOf(MdlAttribute.ResidentCity, "resident_city")
            )
        }
    }

    @RunWith(Parameterized::class)
    class FullDateTests(private val element: MdlAttribute, private val expectedValue: String) {
        @Test
        fun `value matches expected identifier`() = assertEquals(expectedValue, element.value)

        @Test
        fun `validates full-date format`() = assertTrue(element.validate("1980-01-15"))

        @Test
        fun `rejects invalid date format`() = assertFalse(element.validate("15-01-1980"))

        @Test
        fun `rejects empty string`() = assertFalse(element.validate(""))

        @Test
        fun `rejects non-string type`() = assertFalse(element.validate(19800115))

        companion object {
            @JvmStatic
            @Parameters(name = "{1}")
            fun data() = listOf(
                arrayOf(MdlAttribute.BirthDate, "birth_date"),
                arrayOf(MdlAttribute.IssueDate, "issue_date"),
                arrayOf(MdlAttribute.ExpiryDate, "expiry_date")
            )
        }
    }

    class IssuingCountryTests {
        @Test
        fun `value`() = assertEquals("issuing_country", MdlAttribute.IssuingCountry.value)

        @Test
        fun `validates 2 char code`() = assertTrue(MdlAttribute.IssuingCountry.validate("GB"))

        @Test
        fun `rejects 1 char`() = assertFalse(MdlAttribute.IssuingCountry.validate("G"))

        @Test
        fun `rejects 3 chars`() = assertFalse(MdlAttribute.IssuingCountry.validate("GBR"))

        @Test
        fun `rejects non-string`() = assertFalse(MdlAttribute.IssuingCountry.validate(42))
    }

    class PortraitTests {
        @Test
        fun `value`() = assertEquals("portrait", MdlAttribute.Portrait.value)

        @Test
        fun `validates any type`() = assertTrue(MdlAttribute.Portrait.validate(ByteArray(1024)))
    }

    class DrivingPrivilegesTests {
        @Test
        fun `value`() = assertEquals("driving_privileges", MdlAttribute.DrivingPrivileges.value)

        @Test
        fun `validates list`() = assertTrue(MdlAttribute.DrivingPrivileges.validate(listOf("B")))

        @Test
        fun `validates empty list`() =
            assertTrue(MdlAttribute.DrivingPrivileges.validate(emptyList<Any>()))

        @Test
        fun `rejects non-list`() = assertFalse(MdlAttribute.DrivingPrivileges.validate("B"))
    }

    class UnDistinguishingSignTests {
        @Test
        fun `value`() =
            assertEquals("un_distinguishing_sign", MdlAttribute.UnDistinguishingSign.value)

        @Test
        fun `validates non-empty string`() =
            assertTrue(MdlAttribute.UnDistinguishingSign.validate("UK"))

        @Test
        fun `rejects empty string`() = assertFalse(MdlAttribute.UnDistinguishingSign.validate(""))

        @Test
        fun `rejects non-string`() = assertFalse(MdlAttribute.UnDistinguishingSign.validate(123))
    }

    class AgeOverTests {
        @Test
        fun `value formats with zero-padded age`() =
            assertEquals("age_over_18", MdlAttribute.AgeOver(18).value)

        @Test
        fun `accepts boundary 0 with padding`() =
            assertEquals("age_over_00", MdlAttribute.AgeOver(0).value)

        @Test
        fun `pads single digit`() = assertEquals("age_over_01", MdlAttribute.AgeOver(1).value)

        @Test
        fun `accepts boundary 99`() = assertEquals("age_over_99", MdlAttribute.AgeOver(99).value)

        @Test
        fun `rejects negative`() {
            assertThrows(IllegalArgumentException::class.java) { MdlAttribute.AgeOver(-1) }
        }

        @Test
        fun `rejects over 99`() {
            assertThrows(IllegalArgumentException::class.java) { MdlAttribute.AgeOver(100) }
        }

        @Test
        fun `validates boolean true`() = assertTrue(MdlAttribute.AgeOver(18).validate(true))

        @Test
        fun `validates boolean false`() = assertTrue(MdlAttribute.AgeOver(18).validate(false))

        @Test
        fun `rejects non-boolean`() = assertFalse(MdlAttribute.AgeOver(18).validate("true"))
    }

    class CustomTests {
        @Test
        fun `value matches provided string`() =
            assertEquals("custom_attr", MdlAttribute.Custom("custom_attr").value)

        @Test
        fun `validates any type`() = assertTrue(MdlAttribute.Custom("x").validate("string"))

        @Test
        fun `validates int`() = assertTrue(MdlAttribute.Custom("x").validate(123))

        @Test
        fun `validates boolean`() = assertTrue(MdlAttribute.Custom("x").validate(true))
    }
}
