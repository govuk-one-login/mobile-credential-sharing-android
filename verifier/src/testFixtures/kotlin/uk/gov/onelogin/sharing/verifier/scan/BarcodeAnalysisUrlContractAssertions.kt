package uk.gov.onelogin.sharing.verifier.scan

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.EXTRA_SHARE_STATE
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf

object BarcodeAnalysisUrlContractAssertions {
    fun hasState(expected: Uri) = allOf(
        hasData(expected),
        hasExtra(
            EXTRA_SHARE_STATE,
            CustomTabsIntent.SHARE_STATE_OFF
        )
    )
}
