package uk.gov.onelogin.sharing.orchestration.prerequisites.contracts

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.ACTION_REQUEST_PERMISSIONS
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

@RunWith(AndroidJUnit4::class)
class PrerequisiteActionContractTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `Requesting permission Intents contain permissions as an extra`() = runTest {
        val permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.CAMERA
        )

        assertThat(
            createIntent(PrerequisiteAction.RequestPermissions(permissions)),
            allOf(
                hasAction(ACTION_REQUEST_PERMISSIONS),
                hasExtra(EXTRA_PERMISSIONS, permissions.toTypedArray())
            )
        )
    }

    @Test
    fun `Opening app settings use the context as Intent data`() = runTest {
        assertThat(
            createIntent(PrerequisiteAction.OpenAppPermissions),
            allOf(
                hasAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS),
                hasData(Uri.fromParts("package", context.packageName, null)),
                hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        )
    }

    @Test
    fun `Enabling bluetooth only has an action`() = runTest {
        assertThat(
            createIntent(PrerequisiteAction.EnableBluetooth),
            hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )
    }

    @Test
    fun `Enabling location services only has an action`() = runTest {
        assertThat(
            createIntent(PrerequisiteAction.EnableLocationServices),
            hasAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        )
    }

    @Test
    fun `Parsing results doesn't interact with resulting Intent`() = runTest {
        val resultIntent: Intent = mockk(relaxed = true)

        PrerequisiteActionContract.parseResult(Activity.RESULT_OK, resultIntent)

        verify { resultIntent wasNot Called }
        confirmVerified(resultIntent)
    }

    private fun createIntent(action: PrerequisiteAction) = PrerequisiteActionContract.createIntent(
        context,
        action
    )
}
