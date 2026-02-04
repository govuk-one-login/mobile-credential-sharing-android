package uk.gov.onelogin.orchestration

import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag

class HolderSession(private val logger: Logger) : Session {
    override fun transitionToState(state: String) {
        logger.debug(logTag, "Transitioning to state: $state")
    }
}
