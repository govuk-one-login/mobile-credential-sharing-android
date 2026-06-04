package uk.gov.onelogin.sharing.verification.format.document.validity

import kotlin.time.ExperimentalTime

/**
 * Values extracted from the IssuerAuth COSE_Sign1 leaf certificate and payload.
 *
 * @param certificateValidityPeriod The leaf certificate's notBefore/notAfter.
 * @param msoPayload The raw MSO payload bytes (Tag-24 wrapped).
 * @param subjectCountry The Subject C attribute from the leaf certificate.
 * @param subjectState The Subject stateOrProvinceName attribute (null when absent).
 */
@OptIn(ExperimentalTime::class)
data class IssuerAuthResult(
    val certificateValidityPeriod: CertificateValidityPeriod,
    val msoPayload: ByteArray,
    val subjectCountry: String,
    val subjectState: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IssuerAuthResult
        if (certificateValidityPeriod != other.certificateValidityPeriod) return false
        if (!msoPayload.contentEquals(other.msoPayload)) return false
        if (subjectCountry != other.subjectCountry) return false
        if (subjectState != other.subjectState) return false
        return true
    }

    override fun hashCode(): Int {
        var result = certificateValidityPeriod.hashCode()
        result = 31 * result + msoPayload.contentHashCode()
        result = 31 * result + subjectCountry.hashCode()
        result = 31 * result + (subjectState?.hashCode() ?: 0)
        return result
    }
}
