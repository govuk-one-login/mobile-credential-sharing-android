package uk.gov.onelogin.sharing.cryptoService.cose

import uk.gov.onelogin.sharing.cryptoService.cose.Cose.ECKeyType.EC2
import uk.gov.onelogin.sharing.cryptoService.cose.Cose.ECKeyType.OKP

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
 * @see [uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto]
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

    enum class ECKeyType(val id: UInt) {
        OKP(1u),
        EC2(2u),
        RSA(3u),
        SYMMETRIC(4u),
        HSS_LMS(5u),
        WALNUT_DSA(6u)
    }

    /**
     * The type of elliptic curve (EC).
     *
     * @param curveId The COSE Key identifier of the curve.
     * @param expectedKeyType The COSE Key type associated with the curve.
     *
     * @see <a href="https://www.iana.org/assignments/cose/cose.xhtml#elliptic-curves">Cose curve types</a>
     */
    enum class ECType(
        val curveId: UInt,
        val expectedKeyType: ECKeyType,
        val parameterSpecName: String,
        expectedUncompressedCoordinateLength: Int
    ) {
        P256(
            curveId = 1u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 256,
            parameterSpecName = "secp256r1"
        ),
        P384(
            curveId = 2u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 384,
            parameterSpecName = "secp384r1"
        ),
        P521(
            curveId = 3u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 521,
            parameterSpecName = "secp521r1"
        ),
        X25519(
            curveId = 4u,
            expectedKeyType = OKP,
            expectedUncompressedCoordinateLength = 256,
            parameterSpecName = "X25519"
        ),
        X448(
            curveId = 5u,
            expectedKeyType = OKP,
            expectedUncompressedCoordinateLength = 448,
            parameterSpecName = "X448"
        ),
        Ed25519(
            curveId = 6u,
            expectedKeyType = OKP,
            expectedUncompressedCoordinateLength = 256,
            parameterSpecName = "Ed25519"
        ),
        Ed448(
            curveId = 7u,
            expectedKeyType = OKP,
            expectedUncompressedCoordinateLength = 448,
            parameterSpecName = "Ed448"
        ),
        BRAINPOOL_P256(
            curveId = 256u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 256,
            parameterSpecName = "brainpoolP256r1"
        ),
        BRAINPOOL_P320(
            curveId = 257u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 320,
            parameterSpecName = "brainpoolP320r1"
        ),
        BRAINPOOL_P384(
            curveId = 258u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 384,
            parameterSpecName = "brainpoolP384r1"
        ),
        BRAINPOOL_P512(
            curveId = 259u,
            expectedKeyType = EC2,
            expectedUncompressedCoordinateLength = 512,
            parameterSpecName = "brainpoolP512r1"
        );

        val keyTypeId: UInt = expectedKeyType.id
        val expectedCoordinateByteLength: Int =
            expectedUncompressedCoordinateLength / ByteHelper.BITS_IN_A_BYTE
    }
}
