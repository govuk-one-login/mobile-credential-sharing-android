package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenBodyV2
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreenV2
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

@Composable
internal fun MoreAboutDvsScreen(
    presentation: ConsentPresentation,
    modifier: Modifier = Modifier,
) {
    MoreAboutDvsContent(
        presentation = presentation,
        modifier = modifier
    )
}

@Composable
internal fun MoreAboutDvsContent(
    presentation: ConsentPresentation,
    modifier: Modifier = Modifier
) {

    val organizationName = presentation.organizationName ?: "Unknown"

    val bodyTexts = listOf(
        R.string.more_about_dvs_body_1,
        R.string.more_about_dvs_body_2,
        R.string.more_about_dvs_body_3,
    ).map { resourceId ->
        stringResource(
            resourceId,
            organizationName
        ).removeDoubleFullStop()
    }.map(LeftAlignedScreenBodyV2::Text)

    val bulletText = listOf(
        R.string.more_about_dvs_bullet_1,
        R.string.more_about_dvs_bullet_2,
        R.string.more_about_dvs_bullet_3,
    ).map { resourceId ->
        stringResource(
            resourceId,
            organizationName
        ).removeDoubleFullStop()
    }.map { bulletText ->
        ListItem(text = bulletText)
    }.let { bulletItems ->
        LeftAlignedScreenBodyV2.BulletList(
            items = bulletItems.toPersistentList()
        )
    }

    val privacyNoticeSubheading = LeftAlignedScreenBodyV2.Title(
        text = stringResource(
            R.string.more_about_dvs_privacy_subheading,
            organizationName
        ),
        modifier = Modifier.padding(top = spacingDouble),
        style = GdsHeadingStyle.Title2,
        textAlign = GdsHeadingAlignment.LeftAligned
    )

    LeftAlignedScreenV2(
        modifier = modifier,
        title = stringResource(
            R.string.more_about_dvs_title,
            organizationName
        ),
        body = (bodyTexts + bulletText + privacyNoticeSubheading)
            .toPersistentList(),
        forceScroll = true,
    )
}

@PreviewContent
@Composable
internal fun MoreAboutDvsPreview(
    @PreviewParameter(provider = MoreAboutDvsPreviewParameters::class)
    presentation: ConsentPresentation
) {
    GdsTheme {
        Surface {
            MoreAboutDvsContent(
                presentation = presentation,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

internal fun String.removeDoubleFullStop(): String = let { text ->
    if (text.endsWith("..")) {
        text.dropLast(1)
    } else {
        text
    }
}

internal class MoreAboutDvsPreviewParameters(
    override val values: Sequence<ConsentPresentation> = sequenceOf(
        ConsentPresentation(
            documents = emptyList(),
            organizationName = "Stub-DVS Ltd."
        )
    )
) : PreviewParameterProvider<ConsentPresentation>