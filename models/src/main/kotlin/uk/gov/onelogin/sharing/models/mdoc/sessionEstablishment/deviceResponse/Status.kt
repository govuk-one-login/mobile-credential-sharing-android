package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

enum class Status(val code: Int) {
    OK(0),
    GENERAL_ERROR(10),
    CBOR_DECODING_ERROR(11),
    CBOR_VALIDATION_ERROR(12);

    companion object {
        private val applicableCodes = Status.entries.map(Status::code)

        fun from(code: Int?): Status {
            require(code in applicableCodes) {
                "Received invalid device response status code: $code"
            }

            return Status.entries.first { it.code == code }
        }
    }
}
