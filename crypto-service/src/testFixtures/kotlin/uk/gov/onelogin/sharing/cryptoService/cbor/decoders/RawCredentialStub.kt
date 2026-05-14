package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

@OptIn(ExperimentalStdlibApi::class)
object RawCredentialStub {
    const val VALID_RAW_CREDENTIAL_HEX =
        "bf6a6e616d65537061636573bfff6a697373756572417574689f4101bfff5824d818" +
            "5820bf67646f6354797065756f72672e69736f2e31383031332e352e312e6d444cff4102ffff"

    // Credential with COSE protected header containing integer key 1 -> integer value -7 (ES256)
    const val CREDENTIAL_WITH_INTEGER_KEYS_HEX =
        "a26a6e616d65537061636573a06a69737375657241757468" +
            "8443a10126a05823d818581fa167646f6354797065756f72672e69736f2e31383031332e352e312e6d444c4102"

    const val ISSUER_AUTH_WITH_INTEGER_KEYS_HEX =
        "8443a10126a05823d818581fa167646f6354797065756f72672e69736f2e31383031332e352e312e6d444c4102"

    val validRawCredentialBytes: ByteArray get() = VALID_RAW_CREDENTIAL_HEX.hexToByteArray()
    val credentialWithIntegerKeysBytes: ByteArray get() =
        CREDENTIAL_WITH_INTEGER_KEYS_HEX.hexToByteArray()
    val issuerAuthWithIntegerKeysBytes: ByteArray get() =
        ISSUER_AUTH_WITH_INTEGER_KEYS_HEX.hexToByteArray()
}
