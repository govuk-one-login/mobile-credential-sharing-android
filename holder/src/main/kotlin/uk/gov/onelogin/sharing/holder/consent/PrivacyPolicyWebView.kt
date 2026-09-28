package uk.gov.onelogin.sharing.holder.consent

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import uk.gov.onelogin.sharing.holder.R

private const val WEBVIEW_STATE_KEY = "WEBVIEW_STATE"

/**
 * A full-screen, embedded [WebView] that displays the verified ReaderAuth privacy-policy [url].
 */

@Composable
internal fun PrivacyPolicyWebView(url: String, onClose: () -> Unit) {
    val webViewStateBundle = rememberSaveable { Bundle() }

    BackHandler(enabled = true) {
        onClose()
    }

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

                            if (webViewStateBundle.containsKey(WEBVIEW_STATE_KEY)) {
                                restoreState(webViewStateBundle.getBundle(WEBVIEW_STATE_KEY)!!)
                            } else {
                                loadUrl(url)
                            }
                        }
                    },
                    onRelease = { releasedWebView ->
                        val bundle = Bundle()
                        releasedWebView.saveState(bundle)
                        webViewStateBundle.putBundle(WEBVIEW_STATE_KEY, bundle)
                    }
                )
            }
        }
    }
}

private fun WebView.configureSecureSettings() {
    settings.apply {
        allowFileAccess = false
        allowContentAccess = false
        domStorageEnabled = false
        cacheMode = LOAD_NO_CACHE
    }
}

/**
 * Restricts navigation to `https` URLs, blocking any attempt to leave the embedded view
 * via other schemes.
 */
private class PrivacyPolicyWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val scheme = request?.url?.scheme?.lowercase()
        return scheme != HTTPS_SCHEME
    }

    private companion object {
        const val HTTPS_SCHEME = "https"
    }
}
