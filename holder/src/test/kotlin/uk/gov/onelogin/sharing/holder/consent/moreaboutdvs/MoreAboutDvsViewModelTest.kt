package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.runner.RunWith
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsTestData.expectedButtonEvent
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsViewModelTest {

    private val analyticsLogger = FakeAnalyticsLogger()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val viewModel by lazy {
        MoreAboutDvsViewModel(
            analyticsLogger = analyticsLogger,
        )
    }

    @Test
    fun `Sends a screen-level analytics event`() = runTest {
        viewModel.sendScreenEvent(context).join()

        assertThat(
            analyticsLogger,
            contains(expectedScreenEvent)
        )
    }

    @Test
    fun `Sends a button-level analytics event`() = runTest {
        viewModel.sendButtonEvent(context).join()

        assertThat(
            analyticsLogger,
            contains(expectedButtonEvent)
        )
    }
}