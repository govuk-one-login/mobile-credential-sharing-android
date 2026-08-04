package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

/**
 * Thrown when a DeviceRequest contains more than the ISO 18013-5 mandated
 * limit of two age_over_NN elements.
 */
class AgeOverNNRequestLimitException(message: String = MESSAGE) : Exception(message) {
    companion object {
        const val MESSAGE =
            "SessionData termination initiated due to exceeding age_over_NN request limit"
    }
}
