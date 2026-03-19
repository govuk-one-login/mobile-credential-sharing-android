package uk.gov.onelogin.sharing.cryptoService.engagement

import java.util.UUID

fun interface GenerateEngagementQrCode {

    fun generateQrCode(uuid: UUID): String
}
