package uk.gov.onelogin.sharing.security.cryptography.java

import uk.gov.onelogin.sharing.security.util.getByteArrayFromFile

object CryptoStub {
    const val BINARY_PACKAGE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/"

    val VALID_SALT_BYTES = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "sessionTranscriptAsSaltBytes.bin"
    )

    val SHARED_SECRET_BYTES = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "exampleSharedSecret.bin"
    )

    val VALID_HKDF_DEVICE_KEY = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "hkdfDeviceKey.bin"
    )

    val VALID_HKDF_READER_KEY = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "hkdfReaderKey.bin"
    )

    val VALID_MESSAGE_AUTHENTICATION_CODE_BYTES = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "validMessageAuthenticationCode.bin"
    )

    val BSB_BYTES = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "testBSB.bin"
    )

    val PRK_BYTES = getByteArrayFromFile(
        BINARY_PACKAGE_PATH,
        "testPseudoRandomKey.bin"
    )
}
