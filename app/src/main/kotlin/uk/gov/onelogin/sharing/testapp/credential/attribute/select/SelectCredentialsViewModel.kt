package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.ReaderAuthCertificateStatus
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.ReaderAuthCertificateValidator
import uk.gov.onelogin.sharing.testapp.verifier.auth.reader.TestAppReaderAuthCredentialProviderFactory

@HiltViewModel
class SelectCredentialsViewModel @Inject constructor(
    private val readerAuthFactory: TestAppReaderAuthCredentialProviderFactory,
    private val certificateValidator: ReaderAuthCertificateValidator
) : ViewModel() {
    val readerAuthOption: StateFlow<ReaderAuthOption> = readerAuthFactory
        .readerAuthOption
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ReaderAuthOption.VALID
        )

    /**
     * `true` when the selected reader-auth certificate is a valid, provisioned
     * certificate; `false` when it is still an unprovisioned placeholder. When
     * `false` the UI warns the user and blocks verification: a real certificate
     * is only obtained by publishing through the deployment pipeline.
     */
    val readerAuthProvisioned: StateFlow<Boolean> = readerAuthOption
        .map { certificateValidator.validate(it) == ReaderAuthCertificateStatus.VALID }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            true
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
