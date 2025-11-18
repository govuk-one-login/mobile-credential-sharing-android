package uk.gov.onelogin.sharing.holder.engagement

import java.util.Base64
import java.util.UUID
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.security.cbor.encode
import uk.gov.onelogin.sharing.security.cose.CoseKey

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
    override fun qrCodeEngagement(key: CoseKey): String {
        val eDeviceKey = key.encode()
        val uuid = UUID.fromString("11111111-2222-3333-4444-555555555555")
        val securityObject = Security(
            cipherSuiteIdentifier = 1,
            eDeviceKeyBytes = eDeviceKey
        )

        val deviceEngagement = DeviceEngagement.builder(securityObject)
            .version("1.0")
            .ble(peripheralUuid = uuid)
            .build()

        val bytes = deviceEngagement.encode()
        val base64 = Base64.getUrlEncoder().encodeToString(bytes)
        println(base64)

        // for testing purposes - remove when verifier built
        decodeDeviceEngagement(base64)

        return base64
    }
}
