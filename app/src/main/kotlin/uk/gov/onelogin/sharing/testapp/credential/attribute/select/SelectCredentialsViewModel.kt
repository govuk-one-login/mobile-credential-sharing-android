package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@HiltViewModel
class SelectCredentialsViewModel @Inject constructor(
    private val readerAuthFactory: TestAppReaderAuthCredentialProviderFactory,
) : ViewModel() {
    val readerAuthOption: StateFlow<ReaderAuthOption> = readerAuthFactory
        .readerAuthOption
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ReaderAuthOption.VALID
        )

    fun update(option: ReaderAuthOption) =
        viewModelScope.launch {
            readerAuthFactory.update(option)
        }
}