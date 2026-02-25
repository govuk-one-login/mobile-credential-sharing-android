package uk.gov.onelogin.sharing.core.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope

@ContributesBinding(AppScope::class)
@ContributesBinding(ViewModelScope::class)
open class AndroidPermissionChecker(private val context: Context) : PermissionChecker {
    override fun checkPermissions(permissions: List<String>): PermissionChecker.Response {
        val missingPermissions = permissions
            .map { permission ->
                permission to ContextCompat.checkSelfPermission(context, permission)
            }.filterNot { (_, permissionState) ->
                PackageManager.PERMISSION_GRANTED == permissionState
            }.map { (permission, _) ->
                permission
            }

        return if (missingPermissions.isEmpty()) {
            PermissionChecker.Response.Passed
        } else {
            PermissionChecker.Response.Missing(missingPermissions)
        }
    }
}
