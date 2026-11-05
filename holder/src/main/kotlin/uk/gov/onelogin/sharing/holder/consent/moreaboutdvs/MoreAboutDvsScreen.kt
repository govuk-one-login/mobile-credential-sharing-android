package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

@Composable
internal fun MoreAboutDvsScreen(
    modifier: Modifier = Modifier,
    viewModel: MoreAboutDvsViewModel = metroViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sendScreenEvent(context)
    }

    MoreAboutDvsContent(
        state = uiState,
        modifier = modifier
    ) { privacyPolicyUrl ->
        scope.launch {
            viewModel.sendButtonEvent(context)
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

@Composable
internal fun MoreAboutDvsContent(
    state: MoreAboutDvsViewModel.MoreAboutDvsUiState,
    modifier: Modifier = Modifier,
    onPrivacyPolicyLinkClick: (String) -> Unit = {}
) {
    when (state) {
        is MoreAboutDvsViewModel.MoreAboutDvsUiState.Loading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GdsProgressIndicator()
            }
        }

        is MoreAboutDvsViewModel.MoreAboutDvsUiState.Loaded -> {
            LeftAlignedScreenV2(
                modifier = modifier,
                title = stringResource(
                    R.string.more_about_dvs_title,
                    state.organizationName
                ),
                body = state.organizationName.moreAboutDvsBodyContent(
                    onPrivacyPolicyLinkClick = {
                        onPrivacyPolicyLinkClick(state.privacyPolicyUrl)
                    }
                ),
                forceScroll = true
            )
        }
    }

}

@Composable
private fun String.moreAboutDvsBodyContent(
    onPrivacyPolicyLinkClick: () -> Unit
): PersistentList<LeftAlignedScreenBodyV2> = (
        introductionTexts() + introductionBullets() + privacyNoticeSubheading() + privacyNoticeBody() +
                privacyNoticeBullets() +
                privacyNoticeButton(
                    onClick = onPrivacyPolicyLinkClick
                )
        ).toPersistentList()

@Composable
private fun String.introductionTexts(): List<LeftAlignedScreenBodyV2.Text> = listOf(
    R.string.more_about_dvs_body_1,
    R.string.more_about_dvs_body_2,
    R.string.more_about_dvs_body_3
).map { resourceId ->
    stringResource(resourceId, this).removeDoubleFullStop()
}.map(LeftAlignedScreenBodyV2::Text)

@Composable
private fun String.introductionBullets(): LeftAlignedScreenBodyV2.BulletList = listOf(
    R.string.more_about_dvs_bullet_1,
    R.string.more_about_dvs_bullet_2,
    R.string.more_about_dvs_bullet_3
).map { resourceId ->
    stringResource(resourceId, this).removeDoubleFullStop()
}.map { bulletText ->
    ListItem(text = bulletText)
}.let { bulletItems ->
    LeftAlignedScreenBodyV2.BulletList(
        items = bulletItems.toPersistentList()
    )
}

@Composable
private fun String.privacyNoticeSubheading(): LeftAlignedScreenBodyV2.Title =
    LeftAlignedScreenBodyV2.Title(
        text = stringResource(R.string.more_about_dvs_privacy_subheading, this),
        modifier = Modifier.padding(top = spacingDouble),
        style = GdsHeadingStyle.Title2,
        textAlign = GdsHeadingAlignment.LeftAligned
    )

@Composable
private fun String.privacyNoticeBody(): LeftAlignedScreenBodyV2.Text = LeftAlignedScreenBodyV2.Text(
    text = stringResource(R.string.more_about_dvs_privacy_body, this)
)

@Composable
private fun String.privacyNoticeBullets(): LeftAlignedScreenBodyV2.BulletList = listOf(
    R.string.more_about_dvs_privacy_bullet_1,
    R.string.more_about_dvs_privacy_bullet_2
).map { resourceId ->
    stringResource(resourceId, this).removeDoubleFullStop()
}.map { bulletText ->
    ListItem(text = bulletText)
}.let { bulletItems ->
    LeftAlignedScreenBodyV2.BulletList(items = bulletItems.toPersistentList())
}

@Composable
private fun String.privacyNoticeButton(
    onClick: () -> Unit
): LeftAlignedScreenBodyV2.SecondaryButton = LeftAlignedScreenBodyV2.SecondaryButton(
    text = stringResource(R.string.more_about_dvs_privacy_policy_link, this),
    onClick = onClick,
    showIcon = true
)

internal fun String.removeDoubleFullStop(): String = let { text ->
    if (text.endsWith("..")) {
        text.dropLast(1)
    } else {
        text
    }
}

@PreviewContent
@Composable
internal fun MoreAboutDvsPreview(
    @PreviewParameter(provider = MoreAboutDvsPreviewParameters::class)
    state: MoreAboutDvsViewModel.MoreAboutDvsUiState
) {
    GdsTheme {
        Surface {
            MoreAboutDvsContent(
                state = state,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

internal class MoreAboutDvsPreviewParameters(
    private val inputs: List<MoreAboutDvsViewModel.MoreAboutDvsUiState> = listOf(
        MoreAboutDvsViewModel.MoreAboutDvsUiState.Loaded(
            ConsentPresentation(
                documents = emptyList(),
                organizationName = "Stub-DVS Ltd.",
                privacyPolicyUrl = "https://www.more.about.dvs/preview"
            )
        ),
        MoreAboutDvsViewModel.MoreAboutDvsUiState.Loading,
    )
) : PreviewParameterProvider<MoreAboutDvsViewModel.MoreAboutDvsUiState> {
    override val values: Sequence<MoreAboutDvsViewModel.MoreAboutDvsUiState>
        get() = inputs.asSequence()

    override fun getDisplayName(index: Int): String? {
        return inputs[index]::class.java.simpleName
    }
}
