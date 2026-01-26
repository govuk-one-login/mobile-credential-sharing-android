package uk.gov.onelogin.sharing.security.secureArea

import uk.gov.onelogin.sharing.security.secureArea.keys.KeyGenerator
import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator

/**
 * Wrapper interface for holding cryptographic operations throughout a User's session.
 */
interface SessionSecurity :
    KeyGenerator.KeyPairGenerator,
    KeyGenerator.PrivateKeyGenerator,
    KeyGenerator.PublicKeyGenerator,
    SharedSecretGenerator
