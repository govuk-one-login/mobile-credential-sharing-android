package uk.gov.onelogin.sharing.models.mdoc.cbor

enum class CborErrors(val errorMessage: String) {
    DECODING_ERROR(
        "CBOR decoding error: SessionEstablishment contains invalid CBOR " +
            "encoding (status code 11 CBOR decoding error)"
    ),
    PARSING_ERROR(
        "CBOR parsing error: SessionEstablishment missing mandatory keys " +
            "(status code 12 CBOR validation error)"
    )
}
