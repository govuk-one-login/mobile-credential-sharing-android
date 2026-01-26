package uk.gov.onelogin.sharing.security.secureArea

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session via .
 *
 * Uses interface delegation to provide the necessary features.
 */
@ContributesBinding(ViewModelScope::class, binding = binding<SessionSecurity>())
class SessionSecurityImpl(
    keyPairGenerator: KeyGenerator.KeyPairGenerator,
    privateKeyGenerator: KeyGenerator.PrivateKeyGenerator,
    publicKeyGenerator: KeyGenerator.PublicKeyGenerator,
    secretGenerator: SharedSecretGenerator
) : SessionSecurity,
    KeyGenerator.Complete,
    KeyGenerator.KeyPairGenerator by keyPairGenerator,
    KeyGenerator.PrivateKeyGenerator by privateKeyGenerator,
    KeyGenerator.PublicKeyGenerator by publicKeyGenerator,
    SharedSecretGenerator by secretGenerator
