package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

enum class Status(val code: UInt) {
    OK(0u),
    GENERAL_ERROR(10u),
    CBOR_DECODING_ERROR(11u),
    CBOR_VALIDATION_ERROR(12u);

    companion object {
        val applicableCodes = Status.entries.map(Status::code)

        fun from(code: UInt?): Status {
            require(code in applicableCodes) {
                "Received invalid device response status code: $code"
            }

            return Status.entries.first { it.code == code }
        }
    }
}
