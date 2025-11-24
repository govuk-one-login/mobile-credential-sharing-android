package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.browser.customtabs.CustomTabsIntent

/**
 * [ActivityResultContract] implementation that opens a [CustomTabsIntent] based on a [Uri]
 * provided at the time of launch.
 */
open class BarcodeAnalysisUrlContract(
    private val onParseResult: (Int, Intent?) -> Unit = { _, _ -> }
) : ActivityResultContract<Uri, Unit>() {
    override fun createIntent(context: Context, input: Uri): Intent = CustomTabsIntent.Builder()
        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        .build()
        .apply {
            this.intent.data = input
        }.intent

    override fun parseResult(resultCode: Int, intent: Intent?) {
        onParseResult(resultCode, intent)
    }
}
