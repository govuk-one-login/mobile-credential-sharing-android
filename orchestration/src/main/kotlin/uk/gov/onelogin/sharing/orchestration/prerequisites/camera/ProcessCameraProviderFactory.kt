package uk.gov.onelogin.sharing.orchestration.prerequisites.camera

import androidx.camera.lifecycle.ProcessCameraProvider
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException

fun interface ProcessCameraProviderFactory {
    @Throws(
        IllegalStateException::class,
        CancellationException::class,
        ExecutionException::class,
        InterruptedException::class
    )
    fun create(): ProcessCameraProvider
}
