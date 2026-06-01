package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.Serializable
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

/**
 * ```
 * DeviceResponse = {
 *   "version" : tstr,
 *   ? "documents" : [+ Document],
 *   "status" : uint
 * }
 * ```
 */
@Serializable
data class DeviceResponse(
    val version: String = "1.0",
    @Serializable(with = DeviceResponseDocumentsSerializer::class)
    val documents: List<VerifiableDocument.WithPresentation>? = null,
    val documentErrors: Map<String, Status>? = null,
    val status: Status = Status.OK
) {
    val documentCount: Int = documents?.size ?: 0

    constructor(
        statusCode: UInt?,
        documents: List<VerifiableDocument.WithPresentation>? = null,
        documentErrors: Map<String, Status>? = null,
        version: String = "1.0"
    ) : this(
        documents = documents,
        documentErrors = documentErrors,
        status = Status.from(statusCode),
        version = version
    )
}
