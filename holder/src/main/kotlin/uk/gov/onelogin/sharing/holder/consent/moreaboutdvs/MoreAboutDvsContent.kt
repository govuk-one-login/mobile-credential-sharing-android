package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.componentsv2.topappbar.GdsTopAppBarDefaults.scrollBehaviour
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState
import uk.gov.onelogin.sharing.core.R as coreR

@Composable
internal fun MoreAboutDvsContent(
    state: HolderConsentUiState.MoreAboutDvs,
    modifier: Modifier = Modifier,
    onScreenLoad: (String) -> Unit = {},
    onCancelScreen: (String) -> Unit = {},
    onPrivacyPolicyLinkClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val metrics = rememberMetricsStateHolder()
    val latestOnScreenLoad by rememberUpdatedState(onScreenLoad)

    LaunchedEffect(Unit) {
        metrics.putScreenState("MoreAboutDvsScreen")
        context.getEnglishString(
            R.string.more_about_dvs_title,
            "DVS"
        ).let(latestOnScreenLoad)
    }

    Column(modifier = modifier) {
        DismissScreenButton(
            modifier = Modifier
                .fillMaxWidth(),
            onCancelScreen = onCancelScreen
        )

        LeftAlignedScreenV2(
            modifier = Modifier.fillMaxSize(),
            title = stringResource(
                R.string.more_about_dvs_title,
                state.normalisedOrganizationName
            ),
            body = state.normalisedOrganizationName.moreAboutDvsBodyContent(
                onPrivacyPolicyLinkClick = {
                    onPrivacyPolicyLinkClick(state.privacyPolicyUrl)
                }
            ),
            forceScroll = true
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DismissScreenButton(
    modifier: Modifier = Modifier,
    onCancelScreen: (String) -> Unit = {}
) {
    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(
                { onCancelScreen("Close") },
                modifier = Modifier
                    .testTag("closeButton")
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    painter = painterResource(coreR.drawable.filled_close),
                    contentDescription = "Close",
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    tint = GdsLocalColorScheme.current.topBarIcon
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = GdsLocalColorScheme.current.topBarScrolledBackground
        ),
        scrollBehavior = scrollBehaviour()
    )
}

@Composable
private fun String.moreAboutDvsBodyContent(
    onPrivacyPolicyLinkClick: () -> Unit
): PersistentList<LeftAlignedScreenBodyV2> = (
        introductionTexts() +
                introductionBullets() +
                privacyNoticeSubheading() +
                privacyNoticeBody() +
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
    showIcon = true,
    modifier = Modifier.testTag("openUri")
)

internal fun String.removeDoubleFullStop(): String = let { text ->
    if (text.endsWith("..")) {
        text.dropLast(1)
    } else {
        text
    }
}
