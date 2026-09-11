package uk.gov.onelogin.sharing.cryptoService.verifier

/**
 * Thrown when the Verifier fails to construct or CBOR-encode the [
 * uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest],
 * including attaching the signed `readerAuth` to the single mDL `DocRequest`.
 */
class DeviceRequestException(message: String, cause: Throwable) : Exception(message, cause)
