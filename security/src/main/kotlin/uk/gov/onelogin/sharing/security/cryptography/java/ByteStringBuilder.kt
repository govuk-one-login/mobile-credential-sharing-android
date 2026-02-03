package uk.gov.onelogin.sharing.security.cryptography.java

import kotlinx.io.bytestring.ByteStringBuilder

fun ByteStringBuilder.append(value: UInt) = apply {
    append((value shr 24).and(0xffU).toByte())
    append((value shr 16).and(0xffU).toByte())
    append((value shr 8).and(0xffU).toByte())
    append((value shr 0).and(0xffU).toByte())
}