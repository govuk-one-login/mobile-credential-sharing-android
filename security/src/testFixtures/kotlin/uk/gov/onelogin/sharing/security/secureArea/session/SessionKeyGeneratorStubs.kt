package uk.gov.onelogin.sharing.security.secureArea.session

import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile

object SessionKeyGeneratorStubs {
    private const val SECURITY_BINARY_PACKAGE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/security/"

    val VALID_SKREADER_BYTES = getByteArrayFromFile(
        SECURITY_BINARY_PACKAGE_PATH,
        "validSkReaderKey.bin"
    )

    val VALID_SKDEVICE_BYTES = getByteArrayFromFile(
        SECURITY_BINARY_PACKAGE_PATH,
        "validSkDeviceKey.bin"
    )
}
