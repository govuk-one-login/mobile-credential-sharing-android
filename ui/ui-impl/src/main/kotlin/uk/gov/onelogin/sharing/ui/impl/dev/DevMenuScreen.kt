package uk.gov.onelogin.sharing.ui.impl.dev

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.gov.android.ui.theme.m3.GdsTheme

@Composable
fun DevMenuScreen(modifier: Modifier = Modifier) {
    var configA by remember { mutableStateOf(false) }
    var configB by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = "Dev Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Config A"
                )
                Checkbox(
                    checked = configA,
                    onCheckedChange = { configA = it }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Config B"
                )
                Checkbox(
                    checked = configB,
                    onCheckedChange = { configB = it }
                )
            }

            OutlinedButton(onClick = {}, modifier = Modifier.padding(top = 64.dp)) {
                Text("Start Holder")
            }

            OutlinedButton(onClick = {}, modifier = Modifier.padding(top = 16.dp)) {
                Text("Start Verifier")
            }
        }
    }
}

@Preview
@Composable
private fun DevMenuScreenPreview() {
    GdsTheme {
        DevMenuScreen()
    }
}
