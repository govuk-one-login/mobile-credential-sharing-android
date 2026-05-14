package uk.gov.onelogin.sharing.cryptoService.util

import java.io.File

fun getByteArrayFromFile(packageName: String, fileName: String): ByteArray = File(
    packageName + fileName
).readBytes()

@OptIn(ExperimentalStdlibApi::class)
fun getByteArrayFromHexStringFile(
    packageName: String,
    fileName: String,
    containsLineBreaks: Boolean = false
): ByteArray {
    val file = File(
        packageName + fileName
    )

    return if (containsLineBreaks) {
        file.readLines().joinToString("")
    } else {
        file.readText()
    }.hexToByteArray()
}
