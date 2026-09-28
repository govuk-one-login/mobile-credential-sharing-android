package uk.gov.onelogin.sharing.holder.consent

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import uk.gov.onelogin.sharing.holder.R

/**
 * A full-screen, embedded [WebView] that displays the verified ReaderAuth privacy-policy [url].
 *
 * The WebView is locked down to reduce risk when loading a Verifier-supplied URL:
 * - JavaScript is enabled (many privacy pages require it to render) but file and content access is
 *   disabled.
 * - Navigation is restricted to `https` URLs; any other scheme (for example `intent://`, `tel:`,
 *   `file:`) is blocked so a redirect cannot escape the WebView or trigger another app.
 */
@Composable
internal fun PrivacyPolicyWebView(url: String, onClose: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextButton(
                onClick = onClose,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = stringResource(R.string.holder_consent_privacy_policy_close))
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = PrivacyPolicyWebViewClient()
                            configureSecureSettings()
                            loadUrl(url)
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.configureSecureSettings() {
    settings.apply {
        javaScriptEnabled = true
        allowFileAccess = false
        allowContentAccess = false
        domStorageEnabled = false
        cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
    }
}

/**
 * Restricts in-WebView navigation to `https` URLs, blocking any attempt to leave the embedded view
 * via other schemes.
 */
private class PrivacyPolicyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val scheme = request?.url?.scheme?.lowercase()
        // Return true to *cancel* loading when the scheme is not https.
        return scheme != HTTPS_SCHEME
    }

    private companion object {
        const val HTTPS_SCHEME = "https"
    }
}
