package uk.gov.onelogin.sharing.bluetooth.internal.core

/**
 * Delay in milliseconds after sending a session end notification,
 * to allow time for the command to be delivered before disconnecting.
 */
internal const val BLE_SEND_NOTIFICATION_DELAY = 200L

/**
 * Byte indicator that more data chunks will be received
 */
internal const val NON_LAST_PART: Byte = 0x01

/**
 * Byte indicator for last data chunk
 */
internal const val LAST_PART: Byte = 0x00
