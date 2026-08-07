package uk.gov.onelogin.sharing.prerequisites.impl.evaluator.camera

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import uk.gov.onelogin.sharing.prerequisites.api.evaluator.camera.ProcessCameraProviderFactory

@ContributesBinding(SharingSessionScope::class)
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
