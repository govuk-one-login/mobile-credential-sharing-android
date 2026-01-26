package uk.gov.onelogin.sharing.security.secureArea

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.security.secureArea.keys.KeyGenerator
import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator

/**
 * An implementation of [SessionSecurity] that handles cryptographic operations for a
 * secure mDoc sharing session.
 */
@ContributesBinding(ViewModelScope::class, binding = binding<SessionSecurity>())
class SessionSecurityImpl(
    keyPairGenerator: KeyGenerator.Complete,
    secretGenerator: SharedSecretGenerator,
) : SessionSecurity,
    KeyGenerator.Complete by keyPairGenerator,
    SharedSecretGenerator by secretGenerator
