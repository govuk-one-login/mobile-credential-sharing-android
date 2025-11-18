package uk.gov.onelogin.sharing.holder

import java.util.UUID
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.security.cose.CoseKey

class FakeEngagementGenerator(private val data: String) : Engagement {
    var coseKey: CoseKey? = null
    var uuid: UUID? = null
    override fun qrCodeEngagement(key: CoseKey, uuid: UUID): String {
        this.coseKey = key
        this.uuid = uuid
        return data
    }
}
