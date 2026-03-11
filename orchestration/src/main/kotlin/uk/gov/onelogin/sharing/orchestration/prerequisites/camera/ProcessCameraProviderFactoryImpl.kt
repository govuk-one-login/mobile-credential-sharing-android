package uk.gov.onelogin.sharing.orchestration.prerequisites.camera

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

@ContributesBinding(AppScope::class)
class ProcessCameraProviderFactoryImpl(private val context: Context) :
    ProcessCameraProviderFactory {
    @Throws(
        IllegalStateException::class,
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    override fun create(): ProcessCameraProvider = ProcessCameraProvider.getInstance(context).get()
}
