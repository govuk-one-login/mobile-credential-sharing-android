package uk.gov.onelogin.sharing.core.permission

import android.content.Context
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.verify

data class ContextCompatStaticMocks(private val context: Context) {
    fun stubPermission(permission: String, result: Int) {
        every {
            ContextCompat.checkSelfPermission(
                context,
                permission
            )
        } returns result
    }

    fun stubAllPermissions(result: Int) {
        every {
            ContextCompat.checkSelfPermission(
                context,
                any()
            )
        } returns result
    }

    fun verifyCheckSelfPermissionInteractions(expectedInteractions: Int = 2) {
        verify(exactly = expectedInteractions) {
            ContextCompat.checkSelfPermission(
                context,
                any()
            )
        }
    }
}
