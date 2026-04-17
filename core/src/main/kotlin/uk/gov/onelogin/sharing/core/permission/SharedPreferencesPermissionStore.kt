package uk.gov.onelogin.sharing.core.permission

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Stores previously requested permissions within the [preferences] [SharedPreferences].
 *
 * This enhances the permission checking workflow by disambiguating between:
 * - Permissions that haven't been requested by the app yet
 * - Permanently denied permissions (not granted; shouldn't show rationale)
 */
class SharedPreferencesPermissionStore(private val preferences: SharedPreferences) :
    PermissionDenialMarkerStore {

    /**
     * Convenience constructor that accepts an application [Context].
     *
     * Passes a private [SharedPreferences] instance, using the proceeding structure for the name:
     *
     * `${contextPackage}.permission.denial.store`
     */
    constructor(context: Context) : this(
        context.getSharedPreferences(
            "${context.packageName}.permission.denial.store",
            Context.MODE_PRIVATE
        )
    )

    override operator fun contains(permission: String): Boolean = this[permission]

    /**
     * @return The [Boolean] value stored with the associated [permission] key. Defaults to `false`
     * when the [permission] key isn't currently stored within the [preferences].
     */
    operator fun get(permission: String): Boolean = preferences.getBoolean(permission, false)

    override fun mark(permission: String) {
        preferences.edit { putBoolean(permission, true) }
    }

    override fun clear(permission: String) {
        preferences.edit { remove(permission) }
    }
}
