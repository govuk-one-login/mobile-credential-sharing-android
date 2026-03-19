package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

fun interface DeriveUntaggedCbor {
    /**
     * Extracts a raw ByteArray from a CBOR-tagged byte string.
     *
     * @param tagged The input [ByteArray].
     * @return The raw, untagged [ByteArray].
     * @throws IllegalArgumentException if the input data is not a CBOR structure
     *         containing tag 24.
     */
    fun deriveUntaggedCbor(tagged: ByteArray): ByteArray
}
