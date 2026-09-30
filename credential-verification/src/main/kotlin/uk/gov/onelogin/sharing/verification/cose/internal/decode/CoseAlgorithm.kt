package uk.gov.onelogin.sharing.verification.cose.internal.decode

/**
 * COSE Algorithm identifiers supported for mdoc / reader authentication (ISO 18013-5).
 */
internal enum class CoseAlgorithm(
    val id: Long,
    val jcaAlgorithmName: String,
    val signatureLength: Int,
    val curveBitLength: Int
) {
    ES256(
        id = -7L,
        jcaAlgorithmName = "SHA256withECDSA",
        signatureLength = 64,
        curveBitLength = 256
    ),
    ES384(
        id = -35L,
        jcaAlgorithmName = "SHA384withECDSA",
        signatureLength = 96,
        curveBitLength = 384
    );

    companion object {
        fun fromId(id: Long): CoseAlgorithm? = entries.firstOrNull { it.id == id }
    }
}
