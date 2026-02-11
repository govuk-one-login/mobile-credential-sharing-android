package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

/**
 * Represents the different states of an mdoc session
 *
 * @param code The byte code representing the state.
 */
internal enum class MdocState(val code: Byte) {
    /**
     * The initial state, indicating the start of a session.
     */
    START(0x01),

    /**
     * The end state, signalling the intent to finish or terminate a session. The mdoc reader shall
     * use this value to signal the end of data retrieval. If used for session termination, ensure
     * the session is not terminated while transmission of SessionData message is ongoing.
     */
    END(0x02);

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
