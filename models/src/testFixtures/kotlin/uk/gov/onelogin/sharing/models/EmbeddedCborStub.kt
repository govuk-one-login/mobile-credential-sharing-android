package uk.gov.onelogin.sharing.models

import uk.gov.onelogin.sharing.models.BleRetrievalStub.UUID
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG

object EmbeddedCborStub {
    val EXPECTED_PREFIX = byteArrayOf(
        0xd8.toByte(),
        EMBEDDED_CBOR_TAG.toByte(),
        0x58.toByte(),
        0x24.toByte()
    )
    val EXPECTED_BYTES = EXPECTED_PREFIX + UUID.toByteArray()
}
