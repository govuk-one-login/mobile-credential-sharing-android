package uk.gov.onelogin.sharing.cryptoService.cryptography.java

import kotlinx.io.bytestring.ByteStringBuilder
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.BigEndianBytes.BYTE_MASK
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.BigEndianBytes.ONE_BYTE
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.BigEndianBytes.THREE_BYTES
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.BigEndianBytes.TWO_BYTES
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.BigEndianBytes.ZERO_BYTES

fun ByteStringBuilder.append(value: UInt) = apply {
    append((value shr THREE_BYTES).and(BYTE_MASK).toByte())
    append((value shr TWO_BYTES).and(BYTE_MASK).toByte())
    append((value shr ONE_BYTE).and(BYTE_MASK).toByte())
    append((value shr ZERO_BYTES).and(BYTE_MASK).toByte())
}

private object BigEndianBytes {
    const val ZERO_BYTES = 0
    const val ONE_BYTE = 8
    const val TWO_BYTES = 16
    const val THREE_BYTES = 24
    const val BYTE_MASK = 0xFFu
}
