package uk.gov.onelogin.sharing.models.mdoc.sessionData

import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

object SessionDataDtoStubs {
    val dataFieldName = CborMapper.default.writeValueAsBytes("data").toHexString()
    val statusFieldName = CborMapper.default.writeValueAsBytes("status").toHexString()

    val emptySessionDataDto = SessionDataDto()

    /**
     * Uses the [ClassLoader] from [SessionDataDtoStubs] to obtain the `sessionDataExample.txt`
     * file contents within the java `resources` directory, so that external gradle modules
     * are capable of accessing this information.
     */
    val validSessionDataDtoBytes: ByteArray = this::class.java.classLoader!!.getResourceAsStream(
        "uk/gov/onelogin/sharing/models/mdoc/sessionData/sessionDataExample.txt"
    )!!.bufferedReader().readLines().joinToString("").hexToByteArray()
}
