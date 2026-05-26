package uk.gov.onelogin.sharing.verification.document.models

import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.ClassInfoExt.scanResult
import uk.gov.onelogin.sharing.verification.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.document.result.VerificationResultMatchers.hasError

@OptIn(ExperimentalTime::class)
class MobileSecurityObjectTest {

    private val now = Clock.System.now()

    /**
     * DCMAW-20245: AC11: [DeviceKeyInfo] and [ValidityInfo] can be constructed independently and
     * embedded within MobileSecurityObject.
     */
    private val mso = MobileSecurityObject(
        docType = "Unit test",
        valueDigests = emptyMap(),
        deviceKeyInfo = DeviceKeyInfo(
            deviceKey = byteArrayOf()
        ),
        validityInfo = ValidityInfo(
            signed = now,
            validFrom = now.minus(2.minutes),
            validUntil = now.plus(2.minutes)
        )
    )

    private val differentDocType = mso.copy(
        docType = "Different"
    )

    private val differentValueDigests = mso.copy(
        valueDigests = mapOf("unit test" to mapOf(1 to byteArrayOf(1, 2)))
    )

    private val differentDeviceKeyInfo = mso.copy(
        deviceKeyInfo = mso.deviceKeyInfo.copy(
            deviceKey = byteArrayOf(1, 2)
        )
    )

    private val differentValidityInfo = mso.copy(
        validityInfo = mso.validityInfo.copy(
            signed = now.minus(2.minutes)
        )
    )

    private val differentStatus = mso.copy(
        status = byteArrayOf(1, 2)
    )

    /**
     * DCMAW-20245: AC10: [MobileSecurityObject] can be constructed with all required fields and
     * correctly implements value equality (including byte array fields).
     */
    @Test
    fun `Equality is based on value, not reference`() {
        assertEquals(mso, mso.copy())
        listOf(
            mso.copy(docType = "Another"),
            mso.copy(valueDigests = mapOf("Test" to emptyMap())),
            mso.copy(deviceKeyInfo = DeviceKeyInfo(deviceKey = byteArrayOf(1, 2))),
            mso.copy(
                validityInfo = mso.validityInfo.copy(
                    signed = now.minus(1.minutes)
                )
            ),
            mso.copy(status = byteArrayOf(2, 3))
        ).forEach {
            assertNotEquals(mso, it)
        }
    }

    /**
     * DCMAW-20245: AC13: [MobileSecurityObject.valueDigests] uses [Int] as the inner key type for
     * digest identifiers.
     */
    @Test
    fun `Value digest Map values is a Map associating Integers with ByteArrays`() {
        val signature = scanResult.getClassInfo(MobileSecurityObject::class.java.name)
            .getFieldInfo("valueDigests")
            .typeSignature
            .toStringWithSimpleNames()

        assertThat(
            signature,
            equalTo("Map<String, Map<Integer, byte[]>>")
        )
    }

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(mso, mso)
        assertEquals(mso, mso.copy())

        assertFalse(mso.equals(null))
        assertFalse(mso.equals("different type"))
        assertNotEquals(mso, differentDocType)
        assertNotEquals(mso, differentValueDigests)
        assertNotEquals(mso, differentDeviceKeyInfo)
        assertNotEquals(mso, differentValidityInfo)
        assertNotEquals(mso, differentStatus)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(mso.hashCode(), mso.copy().hashCode())

        assertNotEquals(mso.hashCode(), differentDocType.hashCode())
        assertNotEquals(mso.hashCode(), differentValueDigests.hashCode())
        assertNotEquals(mso.hashCode(), differentDeviceKeyInfo.hashCode())
        assertNotEquals(mso.hashCode(), differentValidityInfo.hashCode())
        assertNotEquals(mso.hashCode(), differentStatus.hashCode())
    }

    @Test
    fun `Invalid versions throw Verification failures`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            mso.copy(version = "2.0")
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_MSO_VERSION)
        )
    }

    @Test
    fun `Invalid digest algorithms throw Verification failures`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            mso.copy(digestAlgorithm = "NotAnAlgorithm")
        }

        assertThat(
            exception,
            hasError(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM)
        )
    }

    @Test
    fun `Throws a Verification failure when checking against document types`() {
        assertTrue(mso.hasDocType(mso.docType))

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            mso.hasDocType("invalid document")
        }

        assertThat(
            exception,
            hasError(VerificationError.INVALID_DOC_TYPE)
        )
    }
}
