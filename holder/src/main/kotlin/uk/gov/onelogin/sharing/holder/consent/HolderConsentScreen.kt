package uk.gov.onelogin.sharing.holder.consent

import android.net.Uri
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsScreen
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.ReviewWhatYouWillShareContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HolderConsentScreen(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    ),
    viewModel: HolderConsentViewModel = metroViewModel()
) {
    val uiState: HolderConsentUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    HolderConsentContent(
        modifier = modifier,
        state = uiState,
        sheetState = sheetState,
        onAgree = viewModel::onAccept,
        onLoadReviewWhatYouWillShare = viewModel::sendReviewWhatYouWillShareScreenEvent,
        onLoadMoreAboutDvs = viewModel::sendMoreAboutDvsScreenEvent,
        onOpenDvsPrivacyPolicyLink = viewModel::onOpenDvsPrivacyPolicy,
        onReadPrivacyNotice = viewModel::onReadPrivacyNotice,
        onCancel = {
            viewModel.onCancelAndGoBack()
            backDispatcher?.onBackPressed()
        },
        onDismissMoreAboutDvsScreen = viewModel::dismissMoreAboutDvsScreen
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HolderConsentContent(
    state: HolderConsentUiState,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    ),
    onLoadReviewWhatYouWillShare: (String) -> Unit = {},
    onLoadMoreAboutDvs: (String) -> Unit = {},
    onAgree: () -> Unit = {},
    onReadPrivacyNotice: () -> Unit = {},
    onDismissMoreAboutDvsScreen: () -> Unit = {},
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

        is HolderConsentUiState.Loaded -> {
            Column(
                modifier = modifier
            ) {
                ReviewWhatYouWillShareContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onReadPrivacyNotice = onReadPrivacyNotice,
                    onScreenLoad = onLoadReviewWhatYouWillShare,
                    onAgree = onAgree,
                    onCancel = onCancel
                )

                MoreAboutDvsScreen(
                    state = state,
                    sheetState = sheetState,
                    onScreenLoad = onLoadMoreAboutDvs,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                        .padding(top = spacingDouble)
                        .fillMaxSize()
                        .testTag("MoreAboutDvs"),
                    onCancelScreen = onDismissMoreAboutDvsScreen
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
}

@OptIn(ExperimentalMaterial3Api::class)
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
                state = state
            )
        }
    }
}
