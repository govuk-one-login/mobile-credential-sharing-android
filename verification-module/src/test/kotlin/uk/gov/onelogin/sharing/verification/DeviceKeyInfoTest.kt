package uk.gov.onelogin.sharing.verification

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.DeviceKeyInfoMatchers.hasKeyAuthorizations

class DeviceKeyInfoTest {

    private val info = DeviceKeyInfo(
        deviceKey = byteArrayOf()
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
}