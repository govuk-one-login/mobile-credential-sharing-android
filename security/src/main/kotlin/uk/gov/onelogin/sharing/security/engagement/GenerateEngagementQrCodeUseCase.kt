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

/**
 * Use case responsible for generating a secure Engagement QR code string.
 * This is the initial step that establishes connection device to device, allowing a verifier
 * to establish a secure connection.
 *
 * This process involves:
 * 1. Generating a fresh Elliptic Curve (EC) key pair within the [SessionSecurity] context.
 * 2. Converting the generated public key into a COSE Key format.
 * 3. Constructing an engagement structure incorporating the [uuid] and the public key
 * for presentation.
 *
 * @property logger The logging interface for tracking cryptographic operations or errors.
 * @property sessionSecurity Provides secure hardware-backed or software-backed key generation.
 * @property engagementGenerator Logic for formatting the specific engagement data.
 */
@ContributesBinding(AppScope::class, binding = binding<GenerateEngagementQrCode>())
class GenerateEngagementQrCodeUseCase(
    private val logger: Logger,
    private val sessionSecurity: SessionSecurity,
    private val engagementGenerator: Engagement
) : GenerateEngagementQrCode {

    /**
     * Generates a QR code string for session engagement.
     *
     * @param uuid A unique identifier for the session, often used as the transaction/connection ID.
     * @return A string representation of the engagement data, Base64 encoded for display as a
     * QR code.
     */
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
            return engagement
        }
    }
}
