package uk.gov.onelogin.sharing.holder.engagement

import java.util.Base64
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security

/**
 * Generates device engagement data for establishing a connection between mDoc holder
 * and a verifier.
 */

class EngagementGenerator : Engagement {

    /**
     *   Creates an mDoc engagement structure and returns it as a Base64Url encoded string.
     *
     *   @return A [String] containing the Base64Url encoded CBOR representation of Device Engagement
     *   data
     */
    override fun qrCodeEngagement(): String {
        val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()
        val uuid = "11111111-2222-3333-4444-555555555555"
        val securityObject = Security(
            cipherSuiteIdentifier = 1,
            eDeviceKeyBytes = EmbeddedCbor(fakeKeyBytes)
        )

        val deviceEngagement = DeviceEngagement.builder(securityObject)
            .version("1.0")
            .ble(peripheralUuid = uuid)
            .build()

        val bytes = deviceEngagement.encode()
        val base64 = Base64.getUrlEncoder().encodeToString(bytes)

        return base64
    }
}
