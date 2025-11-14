package uk.gov.onelogin.sharing.holder.engagement

import java.util.UUID
import uk.gov.onelogin.sharing.security.cose.CoseKey

fun interface Engagement {

    fun qrCodeEngagement(key: CoseKey, uuid: UUID): String
}
