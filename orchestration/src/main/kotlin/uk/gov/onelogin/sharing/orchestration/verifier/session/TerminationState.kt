package uk.gov.onelogin.sharing.orchestration.verifier.session

/**
 * Represents the states of the ISO 18013-5 §8.3.3.1.3 session termination protocol
 * from the Verifier's perspective.
 */
enum class TerminationState {
    /** No termination in progress. */
    IDLE,

    /** Sending SessionData{status:20} to the holder. */
    SENDING_TERMINATION,

    /** Waiting 500ms for holder to process the termination message. */
    AWAITING_DELAY,

    /** Writing GATT End (0x02) to signal transport closure. */
    SENDING_GATT_END,

    /** Tearing down the BLE connection. */
    STOPPING,

    /** Termination complete. */
    TERMINATED
}
