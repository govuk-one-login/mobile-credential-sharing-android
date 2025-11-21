package uk.gov.onelogin.sharing.security.cbor

/**
 * Defines standardized integer keys for properties within a CBOR map.
 *
 * This object centralizes key definitions for use with Jackson CBOR serializers. Using small
 * integer keys instead of string keys is a best practice to minimize the final encoded data size,
 * making it more efficient for transmission over bluetooth.
 *
 * @see uk.gov.onelogin.sharing.security.cbor.serializers.BleOptionsSerializer for an example of
 * how these keys are used.
 */
object CborPropertyIds {
    const val PROPERTY_ID_0: Long = 0
    const val PROPERTY_ID_1: Long = 1
    const val PROPERTY_ID_2: Long = 2
    const val PROPERTY_ID_10: Long = 10
}
