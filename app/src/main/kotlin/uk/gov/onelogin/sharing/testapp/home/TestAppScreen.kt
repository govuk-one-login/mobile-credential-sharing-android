package uk.gov.onelogin.sharing.testapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.gov.onelogin.sharing.testapp.R

@Composable
fun TestAppScreen(
    modifier: Modifier = Modifier,
    onStartHolderJourney: () -> Unit = {},
    onStartVerifierJourney: () -> Unit = {}
) {
    Scaffold(modifier = modifier) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.test_screen_title),
                modifier = Modifier.padding(bottom = 64.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onStartHolderJourney,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.holder))
            }

            OutlinedButton(
                onClick = onStartVerifierJourney,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.verifier))
            }
        }
    }
}

@Preview
@Composable
private fun TestAppScreenContentPreview() {
    TestAppScreen()
}
