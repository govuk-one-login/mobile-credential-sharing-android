package uk.gov.onelogin.sharing.verification.document

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.validity.ValidityInfo

@OptIn(ExperimentalTime::class)
object MobileSecurityObjectStub {

    fun create(
        docType: String = MobileSecurityObject.DOC_TYPE,
        digestAlgorithm: String = MobileSecurityObject.MSO_DIGEST_ALGORITHM,
        version: String = MobileSecurityObject.MSO_SCHEMA_VERSION,
        valueDigests: Map<String, Map<Int, ByteArray>> = emptyMap()
    ) = MobileSecurityObject(
        docType = docType,
        digestAlgorithm = digestAlgorithm,
        version = version,
        valueDigests = valueDigests,
        deviceKeyInfo = DeviceKeyInfo(deviceKey = byteArrayOf()),
        validityInfo = ValidityInfo(
            signed = Instant.fromEpochSeconds(0),
            validFrom = Instant.fromEpochSeconds(0),
            validUntil = Instant.fromEpochSeconds(0)
        )
    )
}
