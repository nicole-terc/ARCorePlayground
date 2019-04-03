package nstv.arcoreplayground.common

import android.graphics.Bitmap
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.Toast
import com.google.ar.sceneform.ArSceneView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Nicole Terc on 3/20/19.
 */

fun ArSceneView.takePhoto(listener: (String) -> Unit) = savePicture(this, listener)

fun savePicture(sceneView: ArSceneView, listener: (String) -> Unit) {
    val filename = generateFilename()

    // Create a bitmap the size of the scene sceneView.
    val bitmap = Bitmap.createBitmap(
        sceneView.width, sceneView.height,
        Bitmap.Config.ARGB_8888
    )

    // Create a handler thread to offload the processing of the image.
    val handlerThread = HandlerThread("PixelCopier")
    handlerThread.start()

    // Make the request to copy.
    PixelCopy.request(sceneView, bitmap, { copyResult ->
        if (copyResult == PixelCopy.SUCCESS) {
            try {
                saveBitmapToDisk(bitmap, filename)
            } catch (e: IOException) {
                Toast.makeText(sceneView.context, e.toString(), Toast.LENGTH_LONG).show()
                e.printStackTrace()
                return@request
            }
            listener(filename)
        } else {
            Toast.makeText(sceneView.context, "Failed to copyPixels: $copyResult", Toast.LENGTH_LONG).show()
        }
        handlerThread.quitSafely()
    }, Handler(handlerThread.looper))
}

private fun generateFilename(): String {
    val date = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +
            File.separator + "ARCorePlayground/" + date + "_screenshot.jpg"
}


@Throws(IOException::class)
private fun saveBitmapToDisk(bitmap: Bitmap, filename: String) {

    val out = File(filename)
    if (!out.parentFile.exists()) {
        out.parentFile.mkdirs()
    }
    try {
        FileOutputStream(filename).use { outputStream ->
            ByteArrayOutputStream().use { outputData ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData)
                outputData.writeTo(outputStream)
                outputStream.flush()
                outputStream.close()
            }
        }
    } catch (ex: IOException) {
        throw IOException("Failed to save bitmap to disk", ex)
    }

}
