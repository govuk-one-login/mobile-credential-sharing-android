package uk.gov.onelogin.sharing.models.mdoc.transcript

/**
 * ```
 * Handover = QRHandover / NFCHandover / Ext
 * ```
 *
 * @see QR
 * @see NFC
 */
sealed class Handover {
    /**
     * ```
     * QRHandover = null
     * ```
     */
    data object QR : Handover()

    /**
     * ```
     * NFCHandover = [
     *     bstr // Binary value of the Handover Select Message
     *     bstr / null // Binary value of the Handover Request Message, shall be null if NFC
     *                 // Static Handover was used
     * ]
     * ```
     *
     * @param selectMessage Binary value of the Handover Select Message
     * @param requestMessage Binary value of the Handover Request Message.
     */
    data class NFC(val selectMessage: ByteArray, val requestMessage: ByteArray? = null) :
        Handover() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NFC

            if (!selectMessage.contentEquals(other.selectMessage)) return false
            if (!requestMessage.contentEquals(other.requestMessage)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = selectMessage.contentHashCode()
            result = 31 * result + (requestMessage?.contentHashCode() ?: 0)
            return result
        }
    }
}
