package uk.gov.onelogin.sharing.verifier.verify

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

object VerifierPrerequisitesViewModelExt {
    fun TestScope.monitor(model: VerifierPrerequisitesViewModel) {
        backgroundScope.launch { model.events.collect { } }
    }
}
