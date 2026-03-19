package uk.gov.onelogin.sharing.cryptoService.secureArea

import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.SharedSecretGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionEncryption
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator

/**
 * Wrapper interface for holding cryptographic operations throughout a User's session.
 */
interface SessionSecurity :
    KeyPairGenerator,
    SessionKeyGenerator,
    SharedSecretGenerator,
    SessionEncryption
