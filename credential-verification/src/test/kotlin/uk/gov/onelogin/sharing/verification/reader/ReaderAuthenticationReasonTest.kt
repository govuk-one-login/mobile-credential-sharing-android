package uk.gov.onelogin.sharing.verification.reader

import io.github.classgraph.FieldInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult

class ReaderAuthenticationReasonTest {

    @Test
    fun `ReaderAuthenticationReason has all expected enum values`() {
        val expectedValues = setOf(
            "READER_AUTH_MISSING",
            "INVALID_READER_SIGNATURE",
            "MALFORMED_READER_AUTH",
            "UNSUPPORTED_READER_AUTH_ALGORITHM",
            "UNTRUSTED_READER_CERTIFICATE",
            "PRIVACY_POLICY_URL_INVALID",
            "MALFORMED_DEVICE_REQUEST"
        )

        val classInfo = scanResult.getClassInfo(ReaderAuthenticationReason::class.java.name)
        val enumValues = classInfo.enumConstants.map(FieldInfo::getName).toSet()

        assertThat(enumValues, equalTo(expectedValues))
    }

    @Test
    fun `enum values can be converted to and from string`() {
        ReaderAuthenticationReason.entries.forEach { reason ->
            assertEquals(reason, ReaderAuthenticationReason.valueOf(reason.name))
        }
    }
}
