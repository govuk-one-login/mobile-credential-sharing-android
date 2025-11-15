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
    currentDestination: PrimaryTabDestination,
    destinations: List<PrimaryTabDestination>,
    modifier: Modifier = Modifier,
    updateCurrentDestination: (PrimaryTabDestination) -> Unit = {},
    onNavigate: (Any) -> Unit = {}
) {
    PrimaryTabRow(
        selectedTabIndex = destinations.indexOf(currentDestination),
        modifier = modifier.fillMaxWidth()
    ) {
        destinations.forEach { destination ->
            Tab(
                selected = destination == currentDestination,
                onClick = {
                    onNavigate(destination)
                    updateCurrentDestination(destination)
                },
                text = {
                    Text(
                        text = destination.label,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
