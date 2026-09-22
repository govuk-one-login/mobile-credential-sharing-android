package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest

/**
 * Result of successful privacy metadata validation containing the candidate [DocRequest]
 * and the verified privacy-policy URI.
 */
data class AuthenticatedReaderRequest(val docRequest: DocRequest, val privacyPolicyUrl: Uri)
