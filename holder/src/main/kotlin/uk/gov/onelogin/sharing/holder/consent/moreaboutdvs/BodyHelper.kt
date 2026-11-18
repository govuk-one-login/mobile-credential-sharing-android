package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2.Text
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.holder.R

internal object BodyHelper {

    @Composable
    internal fun String.moreAboutDvsBodyContent(
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
    private fun String.introductionTexts(): List<Text> = listOf(
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
    private fun String.privacyNoticeBody(): Text = Text(
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

}