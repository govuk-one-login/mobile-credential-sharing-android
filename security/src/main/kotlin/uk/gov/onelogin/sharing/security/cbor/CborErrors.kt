package uk.gov.onelogin.sharing.security.cbor

/**
 * Defines a set of standardized error messages for CBOR failures
 *
 * Each enum constant represents a specific type of error that can occur during the
 * deserialization or validation of a CBOR data structure, to be used in exceptions or logs.
 *
 * @property errorMessage The error message associated with the error type.
 * This message temporarily includes the status code for debugging purposes
 */
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
