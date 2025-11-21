package uk.gov.onelogin.sharing.verifier.scan.state.permission

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * [PreviouslyDeniedPermissionState.Complete] implementation that defers to the internal [state].
 */
data class MutablePreviouslyDeniedPermissionState(
    private val state: MutableStateFlow<Boolean> = MutableStateFlow(false)
) : PreviouslyDeniedPermissionState.Complete {
    override val hasPreviouslyDeniedPermission: StateFlow<Boolean> = state
    override fun update(hasPreviouslyDeniedPermission: Boolean) {
        state.update { hasPreviouslyDeniedPermission }
    }
}
