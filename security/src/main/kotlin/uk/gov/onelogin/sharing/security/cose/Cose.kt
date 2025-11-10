package uk.gov.onelogin.sharing.security.cose

/**
 * A container for COSE constants, specifically the integer labels used in COSE_Key objects
 * as defined by the IANA COSE registry and RFC 9052.
 *
 * These constants are used as keys in a CBOR map to identify the different parameters
 * of a cryptographic key in a standardized, compact, and interoperable way.
 *
 * @see <a href="https://www.iana.org/assignments/cose/cose.xhtml#key-type-parameters">
 *     IANA COSE Key Type Parameters Registry</a>
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9052">RFC 9052</a>
 */
object Cose {
    /**
     * COSE Key parameter label for 'Kty' Key Type
     *
     * This parameter is used to identify the family of keys for this structure and, thus,
     * the set of key-type-specific parameters to be found.
     */
    const val KEY_KTY_LABEL: Long = 1

    /**
     * COSE Key Common Parameter label for 'kid' (Key Identification).
     *
     * An optional byte string used to uniquely identify the key.
     */
    const val KEY_KID_LABEL: Long = 2

    /**
     * COSE Key Type value for 'EC2' (Elliptic Curve Keys).
     *
     * This is the value associated with the `KEY_KTY_LABEL` when the key is an
     * Elliptic Curve key represented by its curve and public point coordinates (x, y).
     *
     */
    const val KEY_TYPE_EC2: Long = 2

    /**
     * COSE Key Type Parameter label for an Elliptic Curve key that identifies the curve.
     * The value for this key specifies the curve, e.g., '1' for P-256.
     *
     * In this instance negative 1 is the identifier for EC curves
     */
    const val EC_CURVE_LABEL: Long = -1

    /**
     * COSE Elliptic Curve identifier for 'P-256' (NIST P-256 curve, also known as secp256r1).
     * This is the value associated with the `EC_CURVE_LABEL` to specify this curve.
     */
    const val CURVE_P256: Long = 1

    /**
     * COSE Key Type Parameter label for the 'x' coordinate of an Elliptic Curve public key.
     *
     * The value associated with this key is a byte string representing the x-coordinate.
     */
    const val EC_X_COORDINATE_LABEL: Long = -2

    /**
     * COSE Key Type Parameter label for the 'y' coordinate of an Elliptic Curve public key.
     *
     * The value associated with this key is a byte string representing the y-coordinate.
     */
    const val EC_Y_COORDINATE_LABEL: Long = -3
}
