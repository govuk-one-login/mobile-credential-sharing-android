package uk.gov.onelogin.sharing.verification.models

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.verification.models.DeviceKeyInfoMatchers.hasKeyAuthorizations

class DeviceKeyInfoTest {

    private val info = DeviceKeyInfo(
        deviceKey = byteArrayOf()
    )
    private val copy = info.copy()
    private val differentDeviceKey = info.copy(deviceKey = byteArrayOf(0, 1))
    private val differentAuthorizations = info.copy(
        keyAuthorizations = mapOf(
            "unit" to "test"
        )
    )

    /**
     * DCMAW-20245: AC12: [DeviceKeyInfo.keyAuthorizations] is nullable and defaults to null when
     * absent.
     */
    @Test
    fun `Key authorizations are null by default`() {
        assertThat(
            info,
            hasKeyAuthorizations(nullValue())
        )
    }

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(info, info)
        assertEquals(info, copy)

        assertFalse(info.equals(null))
        assertFalse(info.equals("different type"))
        assertNotEquals(info, differentDeviceKey)
        assertNotEquals(info, differentAuthorizations)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(info.hashCode(), copy.hashCode())

        assertNotEquals(info.hashCode(), differentDeviceKey.hashCode())
        assertNotEquals(info.hashCode(), differentAuthorizations.hashCode())
    }
}
