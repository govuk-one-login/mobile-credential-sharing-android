package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParser
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParsingException

class FakeRawCredentialParser : RawCredentialParser {
    var resultToReturn: ParsedRawCredential? = null
    var exceptionToThrow: RawCredentialParsingException? = null

    override fun parse(rawCredential: ByteArray): ParsedRawCredential {
        exceptionToThrow?.let { throw it }
        return resultToReturn
            ?: throw RawCredentialParsingException("No result configured")
    }
}
