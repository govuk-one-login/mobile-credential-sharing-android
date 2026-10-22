@file:Suppress("TooManyFunctions")

package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.theme.m3.Backgrounds
import uk.gov.android.ui.theme.m3.ExtraTypography
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.toMappedColors
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace

private val CARD_CORNER_RADIUS = 16.dp
private val DOCUMENT_ICON_WIDTH = 70.dp
private val DOCUMENT_ICON_HEIGHT = 50.dp
private val DIVIDER_THICKNESS = 1.dp

/** The retention variant for the card, derived from the attributes' `intentToRetain` flags.
 * - [NO_DETAILS_KEPT]: heading, then all attributes, then a retention footer band.
 * - [ALL_DETAILS_KEPT]: heading, then all attributes, then a retention footer band.
 * - [SOME_DETAILS_KEPT]: heading, a "will keep" band, kept attributes, a "will not keep"
 * */
private enum class RetentionVariant { NO_DETAILS_KEPT, ALL_DETAILS_KEPT, SOME_DETAILS_KEPT }

/**
 * Renders the 'Agree to Share' card.
 *
 * @param document The consented document whose namespaces supply the attributes to display.
 * @param organizationName The verified Verifier (DVS) organisation name.
 */
@Composable
internal fun AgreeToShareCard(
    document: ConsentDocument,
    organizationName: String?,
    modifier: Modifier = Modifier
) {
    val resolved = ConsentAttributeResolver.toDisplayAttributes(
        document.namespaces.flatMap(ConsentNamespace::attributes)
    )
    val variant = resolved.toRetentionVariant()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CARD_CORNER_RADIUS))
            .border(
                width = DIVIDER_THICKNESS,
                color = GdsLocalColorScheme.current.dividerCard,
                shape = RoundedCornerShape(CARD_CORNER_RADIUS)
            )
    ) {
        CardHeading(count = resolved.size)

        when (variant) {
            RetentionVariant.NO_DETAILS_KEPT -> {
                SectionDivider()
                AttributeList(resolved)
                SectionDivider()
                RetentionBand {
                    RetentionText(stringResource(R.string.holder_consent_card_none_kept))
                }
            }

            RetentionVariant.ALL_DETAILS_KEPT -> {
                SectionDivider()
                AttributeList(resolved)
                organizationName?.let {
                    SectionDivider()
                    RetentionBand {
                        RetentionText(stringResource(R.string.holder_consent_card_all_kept, it))
                    }
                }
            }

            RetentionVariant.SOME_DETAILS_KEPT -> {
                organizationName?.let {
                    SectionDivider()
                    RetentionBand {
                        RetentionText(stringResource(R.string.holder_consent_card_will_keep, it))
                    }
                    SectionDivider()
                    AttributeList(resolved.filter { attr -> attr.intentToRetain })
                    SectionDivider()
                    RetentionBand {
                        RetentionText(
                            stringResource(R.string.holder_consent_card_will_not_keep, it)
                        )
                    }
                    SectionDivider()
                    AttributeList(resolved.filterNot { attr -> attr.intentToRetain })
                }
            }
        }
    }
}

@Composable
private fun CardHeading(count: Int) {
    val documentName = stringResource(R.string.holder_consent_doc_mdl)
    val headingTemplate = stringResource(R.string.holder_consent_card_heading, count, documentName)

    val bold = SpanStyle(
        fontFamily = ExtraTypography.bodyLargeBold.fontFamily,
        fontWeight = ExtraTypography.bodyLargeBold.fontWeight
    )
    val headingText = buildAnnotatedString {
        append(headingTemplate)
        emphasise(headingTemplate, count.toString(), bold)
        emphasise(headingTemplate, documentName, bold)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GdsLocalColorScheme.current.cardBackground)
            .padding(spacingDouble)
            .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.spacedBy(spacingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.credential_thumbnail_mdl),
            contentDescription = null,
            modifier = Modifier.size(width = DOCUMENT_ICON_WIDTH, height = DOCUMENT_ICON_HEIGHT)
        )
        Text(
            text = headingText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun AttributeList(attributes: List<DisplayAttribute>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Backgrounds.screen.toMappedColors())
    ) {
        attributes.forEachIndexed { index, attribute ->
            AttributeRow(attribute.label())
            if (index != attributes.lastIndex) {
                RowDivider()
            }
        }
    }
}

@Composable
private fun AttributeRow(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacingDouble)
            .semantics(mergeDescendants = true) {}
    )
}

@Composable
private fun RetentionBand(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(GdsLocalColorScheme.current.cardBackground)
            .padding(spacingDouble),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        content()
    }
}

@Composable
private fun RetentionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Start
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        thickness = DIVIDER_THICKNESS,
        color = GdsLocalColorScheme.current.dividerCard
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = spacingDouble),
        thickness = DIVIDER_THICKNESS,
        color = GdsLocalColorScheme.current.dividerCard
    )
}

@Composable
private fun DisplayAttribute.label(): String =
    labelArg?.let { stringResource(labelRes, it) } ?: stringResource(labelRes)

private fun List<DisplayAttribute>.toRetentionVariant(): RetentionVariant = when {
    all { !it.intentToRetain } -> RetentionVariant.NO_DETAILS_KEPT
    all { it.intentToRetain } -> RetentionVariant.ALL_DETAILS_KEPT
    else -> RetentionVariant.SOME_DETAILS_KEPT
}

private fun AnnotatedString.Builder.emphasise(source: String, substring: String, style: SpanStyle) {
    if (substring.isEmpty()) return
    val start = source.indexOf(substring)
    if (start < 0) return
    addStyle(style, start, start + substring.length)
}

private class AgreeToShareCardPreviewProvider : PreviewParameterProvider<Map<String, Boolean>> {
    override val values: Sequence<Map<String, Boolean>> = sequenceOf(
        // No details kept.
        mapOf("family_name" to false, "given_name" to false, "portrait" to false),
        // All details kept.
        mapOf("family_name" to true, "given_name" to true, "document_number" to true),
        // Some details kept.
        mapOf(
            "document_number" to true,
            "family_name" to true,
            "portrait" to false,
            "age_over_18" to false
        )
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AgreeToShareCardPreview(
    @PreviewParameter(AgreeToShareCardPreviewProvider::class) elements: Map<String, Boolean>
) {
    GdsTheme {
        AgreeToShareCard(
            document = ConsentDocument(
                docType = "org.iso.18013.5.1.mDL",
                namespaces = listOf(
                    ConsentNamespace(
                        nameSpace = "org.iso.18013.5.1",
                        attributes = elements.map { (id, retain) -> ConsentAttribute(id, retain) }
                    )
                )
            ),
            organizationName = "Example Organisation.",
            modifier = Modifier.padding(spacingDouble)
        )
    }
}
