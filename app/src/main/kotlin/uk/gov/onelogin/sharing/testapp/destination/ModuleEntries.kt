package uk.gov.onelogin.sharing.testapp.destination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavOptionsBuilder
import kotlinx.collections.immutable.PersistentList
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.theme.smallPadding
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI

@OptIn(UnstableDesignSystemAPI::class)
@Composable
fun ModuleEntries(
    entries: PersistentList<Pair<String, Any>>,
    modifier: Modifier = Modifier,
    onNavigate: (Any, NavOptionsBuilder.() -> Unit) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = modifier.testTag("menuItems")
    ) {
        items(entries) { destination: Pair<String, Any> ->
            GdsHeading(
                text = destination.first,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = {
                        onNavigate(destination.second) {
                            launchSingleTop = true
                        }
                    })
                    .padding(smallPadding),
                textAlign = GdsHeadingAlignment.LeftAligned,
                style = GdsHeadingStyle.Title3
            )
            HorizontalDivider(color = Color.Black)
        }
    }
}
