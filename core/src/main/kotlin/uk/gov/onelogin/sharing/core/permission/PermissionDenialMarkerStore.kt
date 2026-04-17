package uk.gov.onelogin.sharing.core.permission

interface PermissionDenialMarkerStore {
    /**
     * @return `true` when the [permission] has been previously requested within the app.
     * Otherwise, `false`.
     */
    operator fun contains(permission: String): Boolean

    /**
     * Updates the internal storage to add the [permission].
     */
    fun mark(permission: String)

    /**
     * Removes the [permission] key from internal storage.
     */
    fun clear(permission: String)
}
