package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@HiltViewModel
class SelectCredentialsViewModel @Inject constructor(
    private val readerAuthFactory: TestAppReaderAuthCredentialProviderFactory
) : ViewModel() {
    val readerAuthOption: StateFlow<ReaderAuthOption> = readerAuthFactory
        .readerAuthOption
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ReaderAuthOption.VALID
        )

    private val _verifierAttributeOption = MutableStateFlow(
        VerifierAttributeOption.PORTRAIT_AND_AGE_OVER_21
    )

    val verifierAttributeOption: StateFlow<VerifierAttributeOption> = _verifierAttributeOption

    fun update(option: ReaderAuthOption) = viewModelScope.launch {
        readerAuthFactory.update(option)
    }

    fun update(option: VerifierAttributeOption) = viewModelScope.launch {
        _verifierAttributeOption.value = option
    }
}
