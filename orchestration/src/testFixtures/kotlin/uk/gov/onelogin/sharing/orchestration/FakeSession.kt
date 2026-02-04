package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.Session

class FakeSession : Session {
    override fun transitionToState(state: String) {
    }
}
