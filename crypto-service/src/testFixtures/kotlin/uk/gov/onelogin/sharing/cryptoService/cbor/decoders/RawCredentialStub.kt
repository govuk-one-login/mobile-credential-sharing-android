package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

@OptIn(ExperimentalStdlibApi::class)
object RawCredentialStub {
    const val VALID_RAW_CREDENTIAL_HEX =
        "bf6a6e616d65537061636573bfff6a697373756572417574689f4101bfff5824d818" +
            "5820bf67646f6354797065756f72672e69736f2e31383031332e352e312e6d444cff4102ffff"

    val validRawCredentialBytes: ByteArray get() = VALID_RAW_CREDENTIAL_HEX.hexToByteArray()
}
