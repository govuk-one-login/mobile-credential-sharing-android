package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

/**
 * ```
 * DeviceResponse = {
 *   "version" : tstr,
 *   ? "documents" : [+ Document],
 *   "status" : uint
 * }
 * ```
 */
data class DeviceResponse(
    val version: String = "1.0",
    val documents: List<Document>? = null,
    val documentErrors: Map<String, Status>? = null,
    val status: Status = Status.OK
) {
    constructor(
        statusCode: UInt?,
        documents: List<Document>? = null,
        documentErrors: Map<String, Status>? = null,
        version: String = "1.0"
    ) : this(
        documents = documents,
        documentErrors = documentErrors,
        status = Status.from(statusCode),
        version = version
    )
}
