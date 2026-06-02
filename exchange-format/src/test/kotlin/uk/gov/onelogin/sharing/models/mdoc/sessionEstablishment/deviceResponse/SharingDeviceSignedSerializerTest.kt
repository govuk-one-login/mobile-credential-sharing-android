package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSignedStub.sharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSignedStub.sharingDeviceSignedJson

class SharingDeviceSignedSerializerTest {
    @Test
    fun `Successfully encodes to a String`() {
        val result = Json.encodeToString(
            SharingDeviceSignedSerializer(),
            sharingDeviceSigned
        )

        assertThat(
            result,
            equalTo(sharingDeviceSignedJson)
        )
    }

    @Test
    fun `Successfully decodes a String`() {
        val result = Json.decodeFromString(
            SharingDeviceSignedSerializer(),
            sharingDeviceSignedJson
        )

        assertThat(
            result,
            equalTo(sharingDeviceSigned)
        )
    }
}