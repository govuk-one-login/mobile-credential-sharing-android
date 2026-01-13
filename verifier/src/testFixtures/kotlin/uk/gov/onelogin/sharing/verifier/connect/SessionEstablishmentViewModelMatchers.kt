package uk.gov.onelogin.sharing.verifier.connect

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.verifier.connect.matchers.HasConnectWithHolderDeviceState

object SessionEstablishmentViewModelMatchers {
    fun hasUiState(
        matcher: Matcher<ConnectWithHolderDeviceState>
    ): Matcher<SessionEstablishmentViewModel> = HasConnectWithHolderDeviceState(matcher)
}
