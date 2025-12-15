package uk.gov.onelogin.sharing.core

import java.nio.ByteBuffer
import java.util.UUID

object UUIDExtensions {
    fun ByteArray.toUUID(): UUID {
        val buffer = ByteBuffer.wrap(this)
        val high = buffer.getLong()
        val low = buffer.getLong()
        return UUID(high, low)
    }

    fun UUID.toByteArray(): ByteArray {
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(this.mostSignificantBits)
        buffer.putLong(this.leastSignificantBits)
        return buffer.array()
    }


    fun UUID.toBytes(): ByteArray {
        val b = ByteBuffer.wrap(ByteArray(UUID_ARRAY_SIZE))
        b.putLong(mostSignificantBits)
        b.putLong(leastSignificantBits)
        return b.array()
    }

    private const val UUID_ARRAY_SIZE = 16
}
