package uk.gov.onelogin.sharing.security.secureArea

import uk.gov.onelogin.sharing.security.secureArea.secret.SharedSecretGenerator
import uk.gov.onelogin.sharing.security.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator

/**
 * Wrapper interface for holding cryptographic operations throughout a User's session.
 */
interface SessionSecurity :
    KeyGenerator.KeyPairGenerator,
    KeyGenerator.PrivateKeyGenerator,
    KeyGenerator.PublicKeyGenerator,
    SessionKeyGenerator,
    SharedSecretGenerator,
    SessionEncryption
