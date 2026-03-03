package uk.gov.onelogin.sharing.security.usecases

import java.util.UUID
import uk.gov.onelogin.sharing.security.engagement.GenerateEngagementQrCode

class FakeGenerateQrCodeUseCase : GenerateEngagementQrCode {

    override fun generateQrCode(uuid: UUID): String =
        "vwBjMS4wAZ8B2BhYTL8BAiABIVggaEszs-vjvapl1A6R1dNF5hoW-DiItRg" +
            "SNQo5qQCvcaIiWCAKJWxTJz6D60HqSdjEfKjJfJarvHkAfzcS3Q-2OltpKf__Ap-" +
            "fAgG_APUB9ApQT_f4Ld1CTVyyO73Egj_G3P____8="
}
