package uk.gov.onelogin.sharing.cryptoService.secureArea

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.SharedSecretGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session.
 *
 * Uses interface delegation to provide the necessary features.
 */
@ContributesBinding(AppScope::class, binding = binding<SessionSecurity>())
class SessionSecurityImpl(
    keyPairGenerator: KeyPairGenerator,
    secretGenerator: SharedSecretGenerator,
    sessionKeyGenerator: SessionKeyGenerator,
    sessionEncryption: SessionEncryption
) : SessionSecurity,
    KeyPairGenerator by keyPairGenerator,
    SessionKeyGenerator by sessionKeyGenerator,
    SharedSecretGenerator by secretGenerator,
    SessionEncryption by sessionEncryption
