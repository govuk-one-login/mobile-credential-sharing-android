package uk.gov.onelogin.sharing.verifier.scan

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.EXTRA_SHARE_STATE
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

/**
 * Container object for hamcrest [Matcher] assertions on an android [Intent].
 */
object BarcodeAnalysisUrlContractAssertions {
    /**
     * Performs the proceeding validations:
     * - The [Intent.getData] matches the [expected] parameter.
     * - The [Intent]'s [EXTRA_SHARE_STATE] is set to [CustomTabsIntent.SHARE_STATE_OFF].
     */
    fun hasState(expected: Uri): Matcher<Intent?> = allOf(
        hasData(expected),
        hasExtra(
            EXTRA_SHARE_STATE,
            CustomTabsIntent.SHARE_STATE_OFF
        )
    )
}
