package uk.gov.onelogin.sharing.verification.models

/**
 * @see uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
 */
interface DeviceSigned {
    /**
     * The raw Tag-24-encoded DeviceNameSpaces CBOR bytes, used when constructing the
     * DeviceAuthentication structure.
     */
    val deviceNameSpacesBytes: ByteArray

    /**
     * The raw COSE_Sign1 structure from the document's DeviceAuth field, containing the device's
     * signature.
     */
    val deviceSignature: ByteArray
}
