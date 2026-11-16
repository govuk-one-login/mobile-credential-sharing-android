package uk.gov.onelogin.sharing.holder.consent

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsContent
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.ReviewWhatYouWillShareContent

@Composable
internal fun HolderConsentScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderConsentViewModel = metroViewModel()
) {
    val uiState: HolderConsentUiState by viewModel.uiState.collectAsStateWithLifecycle()

    HolderConsentContent(
        modifier = modifier,
        state = uiState,
        onAgree = viewModel::onAccept,
        onLoadReviewWhatYouWillShare = viewModel::sendReviewWhatYouWillShareScreenEvent,
        onLoadMoreAboutDvs = viewModel::sendMoreAboutDvsScreenEvent,
        onOpenDvsPrivacyPolicyLink = viewModel::onOpenDvsPrivacyPolicy,
        onReadPrivacyNotice = viewModel::onReadPrivacyNotice,
        onCancel = viewModel::onCancelAndGoBack,
        onDismissMoreAboutDvsScreen = viewModel::dismissMoreAboutDvsScreen
    )
}

@Composable
internal fun HolderConsentContent(
    state: HolderConsentUiState,
    modifier: Modifier = Modifier,
    onLoadReviewWhatYouWillShare: (String) -> Unit = {},
    onLoadMoreAboutDvs: (String) -> Unit = {},
    onAgree: () -> Unit = {},
    onReadPrivacyNotice: () -> Unit = {},
    onDismissMoreAboutDvsScreen: (String) -> Unit = {},
    onOpenDvsPrivacyPolicyLink: (String) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    when (state) {
        HolderConsentUiState.Loading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GdsProgressIndicator()
            }
        }

        is HolderConsentUiState.ConfirmConsent -> {
            ReviewWhatYouWillShareContent(
                modifier = modifier,
                state = state,
                onReadPrivacyNotice = onReadPrivacyNotice,
                onScreenLoad = onLoadReviewWhatYouWillShare,
                onAgree = onAgree,
                onCancel = onCancel
            )
        }

        is HolderConsentUiState.MoreAboutDvs -> {
            MoreAboutDvsContent(
                state = state,
                onScreenLoad = onLoadMoreAboutDvs,
                modifier = modifier,
                onCancelScreen = onDismissMoreAboutDvsScreen,
            ) { privacyPolicyUrl ->
                scope.launch {
                    context.getEnglishString(
                        R.string.more_about_dvs_privacy_policy_link,
                        "DVS"
                    ).let(onOpenDvsPrivacyPolicyLink)

                    val intent = CustomTabsIntent.Builder()
                        .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
                        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                        .setDownloadButtonEnabled(false)
                        .build()

                    privacyPolicyUrl
                        .let(Uri::parse)
                        .let { uri ->
                            intent.launchUrl(context, uri)
                        }
                }
            }
        }
    }
}

@PreviewContent
@Composable
internal fun HolderConsentScreenPreview(
    @PreviewParameter(provider = HolderConsentScreenPreviewParameters::class)
    state: HolderConsentUiState
) {
    GdsTheme {
        Surface {
            HolderConsentContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
            )
        }
    }
}
