package uk.gov.onelogin.sharing.orchestration.prerequisites.camera

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class ProcessCameraProviderFactoryImpl(private val context: Context) :
    ProcessCameraProviderFactory {
    override fun create(): ProcessCameraProvider = ProcessCameraProvider.getInstance(context).get()
}
