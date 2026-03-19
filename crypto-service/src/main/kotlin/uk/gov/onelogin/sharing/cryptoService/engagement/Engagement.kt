package uk.gov.onelogin.sharing.cryptoService.engagement

import java.util.UUID
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey

fun interface Engagement {

    fun qrCodeEngagement(key: CoseKey, uuid: UUID): String

    companion object {
        const val QR_CODE_SCHEME = "mdoc:"
    }
}
