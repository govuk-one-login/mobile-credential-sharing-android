package uk.gov.onelogin.sharing.cryptoService.engagement

import java.util.UUID
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey

class FakeEngagementGenerator(private val data: String) : Engagement {
    var coseKey: CoseKey? = null
    var uuid: UUID? = null
    override fun qrCodeEngagement(key: CoseKey, uuid: UUID): String {
        this.coseKey = key
        this.uuid = uuid
        return data
    }
}
