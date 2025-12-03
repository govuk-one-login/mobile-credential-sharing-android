package uk.gov.onelogin.sharing.security.engagement

import uk.gov.logging.api.Logger
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

class EngagementGenerator(private val logger: Logger) : Engagement {

    /**
     *   Creates an mDoc engagement structure and returns it as a Base64Url encoded string.
     *
     *   @return A [String] containing the Base64Url encoded CBOR representation of Device Engagement
     *   data
     */
    override fun qrCodeEngagement(key: CoseKey, uuid: UUID): String {
        val eDeviceKey = key.encode()
        val securityObject = Security(
            cipherSuiteIdentifier = 1,
            eDeviceKeyBytes = eDeviceKey
        )

        val deviceEngagement = DeviceEngagement.Companion.builder(securityObject)
            .version("1.0")
            .ble(peripheralUuid = uuid)
            .build()

        val bytes = deviceEngagement.encode()
        val base64 = Base64.getUrlEncoder().encodeToString(bytes)

        // for testing purposes - remove when verifier built
        decodeDeviceEngagement(
            cborBase64Url = base64,
            logger = logger
        )

        return base64
    }
}
