package uk.gov.onelogin.sharing.orchestration.session

/**
 * Data class for storing information about a failed User journey.
 */
data class SessionError(val message: String, val exception: Throwable)
