package uk.gov.onelogin.sharing.bluetooth.internal.core

object MtuValues {
    /** The maximum MTU size
     *
     * Since Android 14 the system will always request value 517 ignoring the requested value
     * https://developer.android.com/about/versions/14/behavior-changes-all#mtu-set-to-517
     *
     * This is still useful for older versions of Android to request the max size
     */
    const val MAX_MTU = 517

    /** The minimum/default MTU size */
    const val MIN_MTU = 23

    /** 3 bytes for headers according to ISO spec */
    const val HEADERS = 3

    /** 1 byte ISO header prepended to each chunk (0x00 = last, 0x01 = more to follow) */
    const val ISO_HEADER = 1

    /** 512 is the max value according to the Bluetooth Core Specification*/
    const val MAX_LENGTH = 512

    /**
     * Calculates the maximum number of data bytes that can be sent in a single Bluetooth LE
     * chunk for a given Maximum Transmission Unit (MTU).
     *
     * It takes into account the Bluetooth attribute protocol (ATT) header overhead and
     * enforces the maximum length constraints defined by the Bluetooth Core Specification.
     *
     * @param mtu The negotiated MTU size. Must be between [MIN_MTU] and [MAX_MTU].
     * @return The maximum number of payload bytes available (MTU minus [HEADERS]),
     * capped at [MAX_LENGTH].
     * NOTE: the returned max includes the (LAST_PART/NON_LAST_PART) indicator byte
     * @throws IllegalArgumentException if the provided [mtu] is outside the valid range.
     */
    fun maxChunkBytes(mtu: Int): Int {
        require(mtu in MIN_MTU..MAX_MTU) {
            "mtu must be between $MIN_MTU and $MAX_MTU"
        }

        return (mtu - HEADERS).coerceAtMost(MAX_LENGTH)
    }

    /**
     * Calculates the maximum number of data bytes per chunk after accounting for both
     * the BLE ATT overhead ([HEADERS]) and the 1-byte ISO header ([ISO_HEADER]).
     *
     * @param mtu The negotiated MTU size. Must be between [MIN_MTU] and [MAX_MTU].
     * @return The maximum data payload size per chunk.
     */
    fun dataChunkSize(mtu: Int): Int = maxChunkBytes(mtu) - ISO_HEADER
}
