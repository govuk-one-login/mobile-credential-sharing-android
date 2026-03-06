package uk.gov.onelogin.sharing.orchestration.prerequisites.camera

import androidx.camera.lifecycle.ProcessCameraProvider

fun interface ProcessCameraProviderFactory {
    fun create(): ProcessCameraProvider
}
