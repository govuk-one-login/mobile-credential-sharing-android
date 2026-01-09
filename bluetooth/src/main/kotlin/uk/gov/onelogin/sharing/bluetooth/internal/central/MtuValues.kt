package uk.gov.onelogin.sharing.bluetooth.internal.central

object MtuValues {
    // 512 is the max value
    // factoring in first 3 bytes for the message header
    const val MAX_POSSIBLE = 509
}
