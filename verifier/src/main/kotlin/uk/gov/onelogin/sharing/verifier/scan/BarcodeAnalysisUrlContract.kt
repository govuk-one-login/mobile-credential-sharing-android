package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.browser.customtabs.CustomTabsIntent

class BarcodeAnalysisUrlContract : ActivityResultContract<Uri, Unit>() {
    override fun createIntent(context: Context, input: Uri): Intent = CustomTabsIntent.Builder()
        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        .build()
        .apply {
            this.intent.data = input
        }.intent

    override fun parseResult(resultCode: Int, intent: Intent?) {
    }
}
