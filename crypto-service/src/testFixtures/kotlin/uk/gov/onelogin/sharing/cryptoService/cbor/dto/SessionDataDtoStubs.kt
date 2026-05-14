package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import uk.gov.onelogin.sharing.cryptoService.util.getByteArrayFromHexStringFile

object SessionDataDtoStubs {
    val emptySessionDataDto = SessionDataDto()
    fun invalidStatusDto() = SessionDataDto(status = UInt.MAX_VALUE)

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
