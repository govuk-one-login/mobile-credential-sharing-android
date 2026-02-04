package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

/**
 * Contains the specific details of the data elements being requested for a document.
 *
 * @property docType The document type identifier as defined in the standard
 * (e.g., "org.iso.18013.5.1.mDL" for a mobile Driving License).
 * @property nameSpaces A nested map representing the requested data elements.
 * - The **Outer Key** (String) is the Namespace (e.g., "org.iso.18013.5.1").
 * - The **Inner Key** (String) is the Data Element Identifier (e.g., "family_name").
 * - The **Boolean Value** if the reader requires the specified field.
 */
data class ItemsRequest(val docType: String, val nameSpaces: Map<String, Map<String, Boolean>>)
