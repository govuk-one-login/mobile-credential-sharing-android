package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import java.io.File

object SessionTranscriptStub {
    val validSessionTranscript: ByteArray =
        (
            "src/testFixtures/resources" +
                "/uk/gov/onelogin/sharing/security/cbor/decoders/session/transcript" +
                "/validSessionEstablishment.txt"
            )
            .let(::File)
            .readBytes()
}
