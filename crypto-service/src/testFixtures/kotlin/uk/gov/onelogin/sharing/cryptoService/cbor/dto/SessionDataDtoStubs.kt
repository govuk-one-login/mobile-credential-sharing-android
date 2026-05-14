package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.util.getByteArrayFromHexStringFile

object SessionDataDtoStubs {
    val dataFieldName = CborMapper.default.writeValueAsBytes("data").toHexString()
    val statusFieldName = CborMapper.default.writeValueAsBytes("status").toHexString()

    val emptySessionDataDto = SessionDataDto()

    /**
     * D.5.1 example SessionData object
     */
    val validSessionDataHexString = getByteArrayFromHexStringFile(
        CBOR_FILE_PATH,
        "sessionDataExample.txt",
        containsLineBreaks = true
    )

    private const val CBOR_FILE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/crypto-service/cbor/dto/"
}
