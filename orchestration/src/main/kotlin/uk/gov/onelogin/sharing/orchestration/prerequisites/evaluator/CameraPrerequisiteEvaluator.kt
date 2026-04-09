package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.Manifest
import android.content.Context
import androidx.camera.core.CameraSelector
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.bluetooth.ContextExt.devicePolicyManager
import uk.gov.onelogin.sharing.core.permission.IterablePermissionsExt.hasPermanentlyDeniedPermissions
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState

@ContributesBinding(AppScope::class, binding = binding<PrerequisiteEvaluator<CameraState>>())
@Inject
class CameraPrerequisiteEvaluator(
    private val context: Context,
    private val factory: ProcessCameraProviderFactory,
    permissionChecker: PermissionCheckerV2
) : PermissionCheckerV2 by permissionChecker,
    PrerequisiteEvaluator<CameraState> {
    override fun evaluate(): CameraState? = evaluatePermissions()
        ?: evaluateSupport()
        ?: evaluateRestrictions()

    private fun evaluatePermissions(): CameraState? =
        checkPermissions(Manifest.permission.CAMERA).let { result ->
            when {
                result.isEmpty() -> null
                result.hasPermanentlyDeniedPermissions() -> CameraState.PermissionDeniedPermanently
                else -> CameraState.PermissionNotGranted
            }
        }

    private fun evaluateSupport(): CameraState? = runCatching {
        factory.create()
    }.mapCatching { provider ->
        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }.fold(
        onSuccess = { condition ->
            if (condition) {
                null
            } else {
                CameraState.Unsupported
            }
        },
        onFailure = { CameraState.Unsupported }
    )

    private fun evaluateRestrictions(): CameraState? = if (
        context.devicePolicyManager?.getCameraDisabled(null) ?: true
    ) {
        CameraState.Restricted
    } else {
        null
    }
}
