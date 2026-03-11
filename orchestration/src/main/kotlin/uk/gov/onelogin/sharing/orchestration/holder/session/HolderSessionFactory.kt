package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.security.interfaces.ECPublicKey
import java.util.UUID
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

/**
 * [SessionFactory] implementation that provides new instances of [HolderSession].
 *
 * @see uk.gov.onelogin.orchestration.HolderOrchestrator
 */
@ContributesBinding(scope = AppScope::class)
class HolderSessionFactory(
    private val logger: Logger,
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement
) : SessionFactory<HolderSession> {
    override fun create(): HolderSession {
        val uuid = UUID.randomUUID()

        val keyPair = sessionSecurity.generateEcKeyPair(
            algorithm = ELLIPTIC_CURVE_ALGORITHM,
            parameterSpec = ELLIPTIC_CURVE_PARAMETER_SPEC
        )

        val cosePublicKey = CoseKey.generateCoseKey(
            publicKey = keyPair?.public as ECPublicKey,
            logger = logger
        )

        val engagement = cosePublicKey.let { coseKey ->
            engagementGenerator.qrCodeEngagement(
                coseKey,
                uuid
            )
        }

        val qrCode = "${Engagement.QR_CODE_SCHEME}$engagement"

        val context = HolderSessionContext(
            sessionUuid = uuid,
            keyPair = keyPair,
            engagement = engagement,
            qrCode = qrCode
        )

        return HolderSessionImpl(
            logger = logger,
            sessionContext = context
        )
    }
}
