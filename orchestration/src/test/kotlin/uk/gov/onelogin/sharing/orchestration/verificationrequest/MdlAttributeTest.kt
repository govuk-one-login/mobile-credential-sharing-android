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
class RequestElementTest {

    @RunWith(Parameterized::class)
    class TextElement150CharTests(
        private val element: RequestElement,
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
                arrayOf(RequestElement.FamilyName, "family_name"),
                arrayOf(RequestElement.GivenName, "given_name"),
                arrayOf(RequestElement.IssuingAuthority, "issuing_authority"),
                arrayOf(RequestElement.DocumentNumber, "document_number"),
                arrayOf(RequestElement.BirthPlace, "birth_place"),
                arrayOf(RequestElement.ResidentAddress, "resident_address"),
                arrayOf(RequestElement.ResidentPostalCode, "resident_postal_code"),
                arrayOf(RequestElement.ResidentCity, "resident_city")
            )
        }
    }

    @RunWith(Parameterized::class)
    class FullDateTests(private val element: RequestElement, private val expectedValue: String) {
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
                arrayOf(RequestElement.BirthDate, "birth_date"),
                arrayOf(RequestElement.IssueDate, "issue_date"),
                arrayOf(RequestElement.ExpiryDate, "expiry_date")
            )
        }
    }

    class IssuingCountryTests {
        @Test
        fun `value`() = assertEquals("issuing_country", RequestElement.IssuingCountry.value)

        @Test
        fun `validates 2 char code`() = assertTrue(RequestElement.IssuingCountry.validate("GB"))

        @Test
        fun `rejects 1 char`() = assertFalse(RequestElement.IssuingCountry.validate("G"))

        @Test
        fun `rejects 3 chars`() = assertFalse(RequestElement.IssuingCountry.validate("GBR"))

        @Test
        fun `rejects non-string`() = assertFalse(RequestElement.IssuingCountry.validate(42))
    }

    class PortraitTests {
        @Test
        fun `value`() = assertEquals("portrait", RequestElement.Portrait.value)

        @Test
        fun `validates any type`() = assertTrue(RequestElement.Portrait.validate(ByteArray(1024)))
    }

    class DrivingPrivilegesTests {
        @Test
        fun `value`() = assertEquals("driving_privileges", RequestElement.DrivingPrivileges.value)

        @Test
        fun `validates list`() = assertTrue(RequestElement.DrivingPrivileges.validate(listOf("B")))

        @Test
        fun `validates empty list`() =
            assertTrue(RequestElement.DrivingPrivileges.validate(emptyList<Any>()))

        @Test
        fun `rejects non-list`() = assertFalse(RequestElement.DrivingPrivileges.validate("B"))
    }

    class UnDistinguishingSignTests {
        @Test
        fun `value`() =
            assertEquals("un_distinguishing_sign", RequestElement.UnDistinguishingSign.value)

        @Test
        fun `validates non-empty string`() =
            assertTrue(RequestElement.UnDistinguishingSign.validate("UK"))

        @Test
        fun `rejects empty string`() = assertFalse(RequestElement.UnDistinguishingSign.validate(""))

        @Test
        fun `rejects non-string`() = assertFalse(RequestElement.UnDistinguishingSign.validate(123))
    }

    class AgeOverTests {
        @Test
        fun `value formats with zero-padded age`() =
            assertEquals("age_over_18", RequestElement.AgeOver(18).value)

        @Test
        fun `accepts boundary 0 with padding`() =
            assertEquals("age_over_00", RequestElement.AgeOver(0).value)

        @Test
        fun `pads single digit`() = assertEquals("age_over_01", RequestElement.AgeOver(1).value)

        @Test
        fun `accepts boundary 99`() = assertEquals("age_over_99", RequestElement.AgeOver(99).value)

        @Test
        fun `rejects negative`() {
            assertThrows(IllegalArgumentException::class.java) { RequestElement.AgeOver(-1) }
        }

        @Test
        fun `rejects over 99`() {
            assertThrows(IllegalArgumentException::class.java) { RequestElement.AgeOver(100) }
        }

        @Test
        fun `validates boolean true`() = assertTrue(RequestElement.AgeOver(18).validate(true))

        @Test
        fun `validates boolean false`() = assertTrue(RequestElement.AgeOver(18).validate(false))

        @Test
        fun `rejects non-boolean`() = assertFalse(RequestElement.AgeOver(18).validate("true"))
    }

    class CustomTests {
        @Test
        fun `value matches provided string`() =
            assertEquals("custom_attr", RequestElement.Custom("custom_attr").value)

        @Test
        fun `validates any type`() = assertTrue(RequestElement.Custom("x").validate("string"))

        @Test
        fun `validates int`() = assertTrue(RequestElement.Custom("x").validate(123))

        @Test
        fun `validates boolean`() = assertTrue(RequestElement.Custom("x").validate(true))
    }
}
