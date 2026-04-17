package uk.gov.onelogin.sharing.core.permission

data class ListPermissionStore(
    private val markedPermissions: MutableList<String> = mutableListOf()
) : PermissionDenialMarkerStore,
    Iterable<String> by markedPermissions {
    override fun clear(permission: String) {
        markedPermissions.remove(permission)
    }

    override fun contains(permission: String): Boolean = permission in markedPermissions

    override fun mark(permission: String) {
        markedPermissions.add(permission)
    }
}
