package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import java.io.File
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper

object SessionDataDtoStubs {
    val dataFieldName = CborMapper.default.writeValueAsBytes("data").toHexString()
    val statusFieldName = CborMapper.default.writeValueAsBytes("status").toHexString()

    val emptySessionDataDto = SessionDataDto()

    /**
     * Uses the [ClassLoader] from [SessionDataDtoStubs] to obtain the `sessionDataExample.txt`
     * file contents within the java `resources` directory, so that external gradle modules
     * are capable of accessing this information.
     */
    val validSessionDataDtoBytes: ByteArray = this::class.java.classLoader?.getResource(
        "uk/gov/onelogin/sharing/crypto-service/cbor/dto/sessionDataExample.txt"
    )?.path?.let(::File)?.readLines()?.joinToString("")?.hexToByteArray()!!

    private const val CBOR_FILE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/crypto-service/cbor/dto/"
}
