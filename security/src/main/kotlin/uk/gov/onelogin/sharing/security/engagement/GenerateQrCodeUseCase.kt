package uk.gov.onelogin.sharing.security.engagement

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import java.security.interfaces.ECPublicKey
import java.util.UUID
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@ContributesBinding(AppScope::class, binding = binding<GenerateEngagementQrCode>())
class GenerateQrCodeUseCase(
    private val logger: Logger,
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement
) : GenerateEngagementQrCode {

    override fun generateQrCode(uuid: UUID): String {
        val keyPair = sessionSecurity.generateEcKeyPair(
            algorithm = ELLIPTIC_CURVE_ALGORITHM,
            parameterSpec = ELLIPTIC_CURVE_PARAMETER_SPEC
        )

        val cosePublicKey = CoseKey.generateCoseKey(
            publicKey = keyPair?.public as ECPublicKey,
            logger = logger
        )

        cosePublicKey.let { coseKey ->
            val engagement = engagementGenerator.qrCodeEngagement(
                coseKey,
                uuid
            )
            println("QR CODE $engagement")
            return engagement
        }
    }
}
