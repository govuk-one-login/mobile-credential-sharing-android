package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderRootOption
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.ReaderRootCertificateProvider

@HiltViewModel
class SelectHolderCredentialsViewModel @Inject constructor(
    private val readerRootCertificateProvider: ReaderRootCertificateProvider
) : ViewModel() {

    val readerRootOption: StateFlow<ReaderRootOption> = readerRootCertificateProvider
        .readerRootOption
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ReaderRootOption.SHARING_TEST_APP_MOCK
        )

    fun update(option: ReaderRootOption) = viewModelScope.launch {
        readerRootCertificateProvider.update(option)
    }
}
