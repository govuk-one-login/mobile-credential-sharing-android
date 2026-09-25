package uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential

/**
 * A single attribute retained by filtering.
 *
 * @param elementIdentifier The resolved element identifier that will be shared (for example
 * `family_name`, or `age_over_21`).
 * @param intentToRetain The intent-to-retain flag declared for this attribute in the DeviceRequest.
 */
data class MatchedAttribute(val elementIdentifier: String, val intentToRetain: Boolean)
