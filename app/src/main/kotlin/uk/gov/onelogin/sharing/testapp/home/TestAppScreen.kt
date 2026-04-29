package uk.gov.onelogin.sharing.testapp.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.testapp.R
import uk.gov.onelogin.sharing.testapp.home.TestAppViewModel.NavigationEvent

@Composable
fun TestAppScreen(
    modifier: Modifier = Modifier,
    viewModel: TestAppViewModel = viewModel(),
    onStartHolderJourney: () -> Unit = {},
    onStartVerifierJourney: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val currentOnStartHolderJourney by rememberUpdatedState(onStartHolderJourney)
    val currentOnStartVerifierJourney by rememberUpdatedState(onStartVerifierJourney)
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("TestAppScreen")
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NavigationEvent.Holder -> currentOnStartHolderJourney()
                is NavigationEvent.Verifier -> currentOnStartVerifierJourney()
            }
        }
    }

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
                onClick = {
                    coroutineScope.launch {
                        viewModel.update(NavigationEvent.Holder)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.holder))
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        viewModel.update(NavigationEvent.Verifier)
                    }
                },
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
