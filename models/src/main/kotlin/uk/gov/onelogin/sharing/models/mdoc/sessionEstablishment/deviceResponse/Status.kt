package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

private const val CODE_OK = 0
private const val CODE_GENERAL_ERROR = 10
private const val CODE_CBOR_DECODING_ERROR = 11
private const val CODE_CBOR_VALIDATION_ERROR = 12

enum class Status(val code: Int) {
    OK(CODE_OK),
    GENERAL_ERROR(CODE_GENERAL_ERROR),
    CBOR_DECODING_ERROR(CODE_CBOR_DECODING_ERROR),
    CBOR_VALIDATION_ERROR(CODE_CBOR_VALIDATION_ERROR)
}
