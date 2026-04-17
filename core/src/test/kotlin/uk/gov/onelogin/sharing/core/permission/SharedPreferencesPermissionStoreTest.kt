package uk.gov.onelogin.sharing.core.permission

import android.Manifest
import android.content.SharedPreferences
import androidx.core.content.edit
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class SharedPreferencesPermissionStoreTest {

    private val preferences: SharedPreferences = mockk(relaxed = true)

    private val store by lazy {
        SharedPreferencesPermissionStore(
            preferences = preferences
        )
    }

    @Before
    fun setUp() {
        every {
            preferences.getBoolean(Manifest.permission.BLUETOOTH, false)
        } returns true
        every {
            preferences.getBoolean(Manifest.permission.CAMERA, false)
        } returns false
    }

    @Test
    fun `Defers to shared preferences for 'contains' operator function`() = runTest {
        assertTrue { Manifest.permission.BLUETOOTH in store }
        assertFalse { Manifest.permission.CAMERA in store }
    }

    @Test
    fun `Edits shared preferences when marking a permission`() = runTest {
        store.mark(Manifest.permission.BLUETOOTH)

        verify {
            preferences.edit { putBoolean(Manifest.permission.BLUETOOTH, true) }
        }
        confirmVerified(preferences)
    }

    @Test
    fun `Edits shared preferences when clearing a permission`() = runTest {
        store.clear(Manifest.permission.BLUETOOTH)

        verify {
            preferences.edit { remove(Manifest.permission.BLUETOOTH) }
        }
        confirmVerified(preferences)
    }
}
