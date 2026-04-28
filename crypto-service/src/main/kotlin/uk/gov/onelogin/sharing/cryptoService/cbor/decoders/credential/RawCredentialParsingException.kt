package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

class RawCredentialParsingException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)