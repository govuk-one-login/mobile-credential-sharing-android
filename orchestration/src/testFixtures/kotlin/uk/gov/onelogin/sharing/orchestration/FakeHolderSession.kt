package uk.gov.onelogin.sharing.orchestration

import uk.gov.onelogin.orchestration.HolderSession

class FakeHolderSession : HolderSession {
    override fun transitionToState(state: String) {
    }
}
