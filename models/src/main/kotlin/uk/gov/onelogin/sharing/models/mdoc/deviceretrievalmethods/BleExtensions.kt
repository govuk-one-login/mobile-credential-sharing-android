package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import java.nio.ByteBuffer
import java.util.UUID

const val SIXTEEN_BYTES = 16

fun UUID.toByteArray(): ByteArray {
    val buffer = ByteBuffer.wrap(ByteArray(SIXTEEN_BYTES))
    buffer.putLong(this.mostSignificantBits)
    buffer.putLong(this.leastSignificantBits)
    return buffer.array()
}
