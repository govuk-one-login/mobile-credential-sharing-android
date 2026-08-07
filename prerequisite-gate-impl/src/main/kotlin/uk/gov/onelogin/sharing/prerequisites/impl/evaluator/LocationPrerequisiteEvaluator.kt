package uk.gov.onelogin.sharing.prerequisites.impl.evaluator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.FEATURE_LOCATION
import androidx.core.location.LocationManagerCompat
import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.prerequisites.api.ContextExt.locationManager
import uk.gov.onelogin.sharing.prerequisites.api.evaluator.PrerequisiteEvaluator
import uk.gov.onelogin.sharing.prerequisites.api.permissions.IterablePermissionsExt.hasPermanentlyDeniedPermissions
import uk.gov.onelogin.sharing.prerequisites.api.permissions.IterablePermissionsExt.hasUndeterminedPermissions
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.api.state.LocationState

@ContributesBinding(SharingSessionScope::class, binding = binding<PrerequisiteEvaluator<LocationState>>())
@Inject
class LocationPrerequisiteEvaluator(
    private val context: Context,
    permissionChecker: PermissionChecker
) : PermissionChecker by permissionChecker,
    PrerequisiteEvaluator<LocationState> {
    override fun evaluate(): LocationState? = evaluatePermissions()
        ?: evaluateSupport()
        ?: evaluateReadiness()

    private fun evaluatePermissions(): LocationState? =
        checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION).let { result ->
            when {
                result.isEmpty() ->
                    null

                result.hasPermanentlyDeniedPermissions() ->
                    LocationState.PermissionDeniedPermanently

                result.hasUndeterminedPermissions() -> LocationState.PermissionUndetermined

                else ->
                    LocationState.PermissionNotGranted
            }
        }

    private fun evaluateSupport(): LocationState? = if (
        context.packageManager.hasSystemFeature(FEATURE_LOCATION)
    ) {
        null
    } else {
        LocationState.Unsupported
    }

    private fun evaluateReadiness(): LocationState? = if (
        context.locationManager?.let(LocationManagerCompat::isLocationEnabled) ?: false
    ) {
        null
    } else {
        LocationState.ServicesDisabled
    }
}
