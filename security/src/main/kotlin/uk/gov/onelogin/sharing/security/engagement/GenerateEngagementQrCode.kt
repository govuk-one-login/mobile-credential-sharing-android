package uk.gov.onelogin.sharing.security.engagement

import java.util.UUID

fun interface GenerateEngagementQrCode {

    fun generateQrCode(uuid: UUID): String
}
