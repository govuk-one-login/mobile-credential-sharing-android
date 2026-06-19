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
        expectedKeyType: ECKeyType,
        expectedXLength: Int,
    ) {
        P256(
            curveId = 1u,
            expectedKeyType = EC2,
            expectedXLength = 256,
        ),
        P384(
            curveId = 2u,
            expectedKeyType = EC2,
            expectedXLength = 384
        ),
        P521(
            curveId = 3u,
            expectedKeyType = EC2,
            expectedXLength = 521
        ),
        X25519(
            curveId = 4u,
            expectedKeyType = OKP,
            expectedXLength = 256
        ),
        X448(
            curveId = 5u,
            expectedKeyType = OKP,
            expectedXLength = 448
        ),
        Ed25519(
            curveId = 6u,
            expectedKeyType = OKP,
            expectedXLength = 256
        ),
        Ed448(
            curveId = 7u,
            expectedKeyType = OKP,
            expectedXLength = 448
        ),
        BRAINPOOL_P256(
            curveId = 256u,
            expectedKeyType = EC2,
            expectedXLength = 256,
        ),
        BRAINPOOL_P320(
            257u,
            EC2,
            expectedXLength = 320
        ),
        BRAINPOOL_P384(
            curveId = 258u, expectedKeyType = EC2,
            expectedXLength = 384
        ),
        BRAINPOOL_P512(
            259u,
            EC2,
            expectedXLength = 512
        );

        val keyTypeId: UInt = expectedKeyType.id
        val expectedXByteLength: Int = expectedXLength / 8
    }
}
