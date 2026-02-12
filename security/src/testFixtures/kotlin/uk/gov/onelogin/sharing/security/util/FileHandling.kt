package uk.gov.onelogin.sharing.security.util

import java.io.File

fun getByteArrayFromFile(packageName: String, fileName: String): ByteArray = File(
    packageName + fileName
).readBytes()

@OptIn(ExperimentalStdlibApi::class)
fun getByteArrayFromHexStringFile(packageName: String, fileName: String): ByteArray = File(
    packageName + fileName
).readText().hexToByteArray()
