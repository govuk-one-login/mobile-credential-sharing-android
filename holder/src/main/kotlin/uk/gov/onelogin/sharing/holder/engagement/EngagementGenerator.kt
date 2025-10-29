package uk.gov.onelogin.sharing.holder.engagement

import java.util.Base64
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class EngagementGenerator : Engagement {

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
