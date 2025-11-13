package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TestWrapperTopBar(
    modifier: Modifier = Modifier,
    currentTabIndex: Int = 0,
    updateTabIndex: (Int) -> Unit = {},
    onNavigate: (Any) -> Unit = {}
) {
    PrimaryTabRow(
        selectedTabIndex = currentTabIndex,
        modifier = modifier.fillMaxWidth()
    ) {
        PrimaryTabDestination.entries().forEachIndexed { index, destination ->
            Tab(
                selected = currentTabIndex == index,
                onClick = {
                    onNavigate(destination.getStartRoute())
                    updateTabIndex(index)
                },
                text = {
                    Text(
                        text = destination.label,
                        maxLines = 2,
                        overflow = TextOverflow.Companion.Ellipsis
                    )
                }
            )
        }
    }
}
