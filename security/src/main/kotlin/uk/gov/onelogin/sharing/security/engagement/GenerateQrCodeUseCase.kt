package uk.gov.onelogin.sharing.security.engagement

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity
import java.util.UUID

@ContributesBinding(AppScope::class, binding = binding<GenerateEngagementQrCode>())
class GenerateQrCodeUseCase(
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement
) : GenerateEngagementQrCode {

    override fun generateQrCode(): String {
        val publicKey = sessionSecurity.generateSessionPublicKey()
        publicKey.let { coseKey ->
            val engagement = engagementGenerator.qrCodeEngagement(
                coseKey,
                UUID.randomUUID()
            )
            println(engagement)
            return engagement
        }
    }
}
