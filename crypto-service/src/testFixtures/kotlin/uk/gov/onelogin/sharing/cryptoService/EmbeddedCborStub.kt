package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.UUID_STRING
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG

object EmbeddedCborStub {
    val EXPECTED_PREFIX = byteArrayOf(
        0xd8.toByte(),
        EMBEDDED_CBOR_TAG.toByte(),
        0x58.toByte(),
        0x24.toByte()
    )
    val EXPECTED_BYTES = EXPECTED_PREFIX + UUID_STRING.toByteArray()
}
