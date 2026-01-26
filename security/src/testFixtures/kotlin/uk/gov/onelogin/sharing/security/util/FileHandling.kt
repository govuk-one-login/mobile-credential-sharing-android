package uk.gov.onelogin.sharing.security.util

import java.io.File

fun getByteArrayFromFile(
    packageName: String,
    fileName: String
): ByteArray {
    return File(
        packageName + fileName
    ).readBytes()
}
