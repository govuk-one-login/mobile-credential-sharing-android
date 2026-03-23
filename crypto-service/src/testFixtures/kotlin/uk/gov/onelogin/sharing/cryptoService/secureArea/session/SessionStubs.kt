package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import uk.gov.onelogin.sharing.cryptoService.util.getByteArrayFromFile

object SessionStubs {
    private const val SECURITY_BINARY_PACKAGE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/crypto-service/"

    val VALID_SKREADER_BYTES = getByteArrayFromFile(
        SECURITY_BINARY_PACKAGE_PATH,
        "validSkReaderKey.bin"
    )

    val VALID_SKDEVICE_BYTES = getByteArrayFromFile(
        SECURITY_BINARY_PACKAGE_PATH,
        "validSkDeviceKey.bin"
    )

    val VALID_DECRYPTED_DATA_BYTES = getByteArrayFromFile(
        SECURITY_BINARY_PACKAGE_PATH,
        "correctlyDecryptedData.bin"
    )
}
