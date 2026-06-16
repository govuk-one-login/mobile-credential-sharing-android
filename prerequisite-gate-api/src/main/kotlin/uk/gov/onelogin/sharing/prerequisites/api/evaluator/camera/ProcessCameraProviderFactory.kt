package uk.gov.onelogin.sharing.prerequisites.api.evaluator.camera

import androidx.camera.lifecycle.ProcessCameraProvider
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

/**
 * Obtains the android-powered device's [ProcessCameraProvider].
 */
fun interface ProcessCameraProviderFactory {
    @Throws(
        IllegalStateException::class,
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    fun create(): ProcessCameraProvider
}
