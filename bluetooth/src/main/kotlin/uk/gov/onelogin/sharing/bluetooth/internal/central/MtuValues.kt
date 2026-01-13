package uk.gov.onelogin.sharing.bluetooth.internal.central

object MtuValues {
    // 512 is the max value according to the ISO specification
    // - factoring in first 3 bytes for the message header this becomes 515
    const val MAX_POSSIBLE = 515
}
