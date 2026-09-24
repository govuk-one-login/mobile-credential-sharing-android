package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class PrivacyPolicyUrlValidatorTest {

    private lateinit var validator: PrivacyPolicyUrlValidator

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val urlString = firstArg<String>()
            val mockUri = mockk<Uri>()
            every { mockUri.toString() } returns urlString
            mockUri
        }
        validator = PrivacyPolicyUrlValidator()
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun `valid HTTPS URL passes validation`() {
        val validUrl = "https://example.gov.uk/privacy"
        val result = validator.validate(validUrl)

        assertNotNull(result)
        assertEquals(validUrl, result.toString())
    }

    @Test
    fun `invalid URLs fail validation`(
        @TestParameter invalidUrl: String = namedTestValues(
            "HTTP scheme" to "http://example.gov.uk/privacy",
            "FTP scheme" to "ftp://example.gov.uk/privacy",
            "Missing scheme" to "example.gov.uk/privacy",
            "Empty host" to "https:///privacy",
            "Contains user info" to "https://user:pass@example.gov.uk/privacy",
            "Contains space" to "https://example.gov.uk/privacy policy",
            "Contains non-ASCII character" to "https://exämple.gov.uk/privacy",
            "Length > 2048 chars" to "https://example.gov.uk/" + "a".repeat(2040)
        )
    ) {
        assertNull(validator.validate(invalidUrl))
    }
}
