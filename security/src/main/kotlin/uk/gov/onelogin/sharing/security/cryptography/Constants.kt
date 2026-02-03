package uk.gov.onelogin.sharing.security.cryptography

object Constants {
    const val ELLIPTIC_CURVE_ALGORITHM = "EC"
    const val ELLIPTIC_CURVE_PARAMETER_SPEC = "secp256r1"
    const val HASH_ALGORITHM_SHA256 = "SHA-256"
    const val MAC_ALGORITHM_SHA256 = "HmacSha256"
    const val HKDF_KEY_SIZE = 32

    const val AES_256_TRANSFORMATION = "AES/GCM/NoPadding"

    const val AES_256_ALGORITHM = "AES"

    const val AES_256_NONCE_LENGTH = 128
    const val NIST_INITIALISATION_VECTOR_SIZE = 16
}
