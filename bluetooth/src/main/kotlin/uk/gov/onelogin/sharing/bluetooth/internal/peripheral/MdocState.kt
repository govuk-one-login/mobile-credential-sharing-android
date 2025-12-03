package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

/**
 * Represents the different states of an mdoc session
 *
 * @param code The byte code representing the state.
 */
enum class MdocState(val code: Byte) {
    /**
     * The initial state, indicating the start of a session.
     */
    START(0x01);

    companion object {
        /**
         * Converts a byte code into an [MdocState].
         *
         * @param byte The byte code to convert.
         * @return The corresponding [MdocState], or `null` if the code is not recognized.
         */
        fun fromByte(byte: Byte): MdocState? = entries.firstOrNull {
            it.code == byte
        }
    }
}
