package uk.gov.onelogin.sharing.models.mdoc.transcript

import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

/**
 * ```
 * SessionTranscript = [
 *     DeviceEngagementBytes / null,
 *     EReaderKeyBytes / null,
 *     Handover
 * ]
 * DeviceEngagementBytes = #6.24(bstr .cbor DeviceEngagement)
 * SessionTranscriptBytes = #6.24(bstr .cbor SessionTranscript)
 * ```
 */
data class SessionTranscript(
    val deviceEngagement: DeviceEngagement?,
    val eReaderKey: ByteArray?,
    val handover: Handover?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SessionTranscript

        if (deviceEngagement != other.deviceEngagement) return false
        if (!eReaderKey.contentEquals(other.eReaderKey)) return false
        if (handover != other.handover) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceEngagement?.hashCode() ?: 0
        result = 31 * result + (eReaderKey?.contentHashCode() ?: 0)
        result = 31 * result + (handover?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun qrHandover(
            deviceEngagement: DeviceEngagement?,
            eReaderKey: ByteArray?
        ) = SessionTranscript(
            deviceEngagement = deviceEngagement,
            eReaderKey = eReaderKey,
            handover = Handover.QR
        )
    }
}

