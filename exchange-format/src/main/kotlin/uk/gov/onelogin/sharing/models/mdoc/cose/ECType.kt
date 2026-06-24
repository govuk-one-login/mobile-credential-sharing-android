package uk.gov.onelogin.sharing.models.mdoc.cose

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
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 256,
        parameterSpecName = "secp256r1"
    ),
    P384(
        curveId = 2u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 384,
        parameterSpecName = "secp384r1"
    ),
    P521(
        curveId = 3u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 521,
        parameterSpecName = "secp521r1"
    ),
    X25519(
        curveId = 4u,
        expectedKeyType = ECKeyType.OKP,
        expectedUncompressedCoordinateLength = 256,
        parameterSpecName = "X25519"
    ),
    X448(
        curveId = 5u,
        expectedKeyType = ECKeyType.OKP,
        expectedUncompressedCoordinateLength = 448,
        parameterSpecName = "X448"
    ),
    Ed25519(
        curveId = 6u,
        expectedKeyType = ECKeyType.OKP,
        expectedUncompressedCoordinateLength = 256,
        parameterSpecName = "Ed25519"
    ),
    Ed448(
        curveId = 7u,
        expectedKeyType = ECKeyType.OKP,
        expectedUncompressedCoordinateLength = 448,
        parameterSpecName = "Ed448"
    ),
    BRAINPOOL_P256(
        curveId = 256u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 256,
        parameterSpecName = "brainpoolP256r1"
    ),
    BRAINPOOL_P320(
        curveId = 257u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 320,
        parameterSpecName = "brainpoolP320r1"
    ),
    BRAINPOOL_P384(
        curveId = 258u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 384,
        parameterSpecName = "brainpoolP384r1"
    ),
    BRAINPOOL_P512(
        curveId = 259u,
        expectedKeyType = ECKeyType.EC,
        expectedUncompressedCoordinateLength = 512,
        parameterSpecName = "brainpoolP512r1"
    );

    val keyTypeId: UInt = expectedKeyType.id
    val expectedCoordinateByteLength: Int =
        expectedUncompressedCoordinateLength / ByteHelper.BITS_IN_A_BYTE
}
