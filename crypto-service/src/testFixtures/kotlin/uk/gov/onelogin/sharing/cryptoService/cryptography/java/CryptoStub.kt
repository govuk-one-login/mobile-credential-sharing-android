package uk.gov.onelogin.sharing.cryptoService.cryptography.java

import uk.gov.onelogin.sharing.cryptoService.util.getByteArrayFromFile

object CryptoStub {
    const val BINARY_PACKAGE_PATH =
        "src/testFixtures/resources/uk/gov/onelogin/sharing/security/cryptography/java/"

    const val VALID_SALT =
        "66ea34bad309a58e255831be8a6a89d8f3d5e730af49ac687c2dee82f3b5041b"

    const val VALID_SK_DEVICE_KEY = "3a9cfc475b558204eb31020f32adf319fd4ef84c" +
        "4a18133538bcbb806d597e08"

    const val VALID_SK_READER_KEY = "6162fb421390f81db15088f5eca6aebe931d0931f" +
        "bfd78ee4f4fc3816b149316"

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
