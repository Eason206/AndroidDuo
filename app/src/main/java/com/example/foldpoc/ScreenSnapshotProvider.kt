package com.example.foldpoc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import java.nio.ByteBuffer

/** One-shot public MediaProjection screen snapshot provider. */
class ScreenSnapshotProvider(private val activity: Activity) : AutoCloseable {
    private val manager = activity.getSystemService(MediaProjectionManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var display: android.hardware.display.VirtualDisplay? = null
    private var callback: ((Bitmap?, String?) -> Unit)? = null

    fun capture(resultCode: Int, permissionData: Intent, result: (Bitmap?, String?) -> Unit) {
        closeCapture()
        callback = result
        val metrics = DisplayMetrics().also { activity.windowManager.defaultDisplay.getRealMetrics(it) }
        val width = metrics.widthPixels.takeIf { it > 0 } ?: activity.resources.displayMetrics.widthPixels
        val height = metrics.heightPixels.takeIf { it > 0 } ?: activity.resources.displayMetrics.heightPixels
        val density = metrics.densityDpi.takeIf { it > 0 } ?: activity.resources.displayMetrics.densityDpi
        val captureReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2)
        reader = captureReader
        captureReader.setOnImageAvailableListener({ source ->
            val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
            image.use {
                val plane = it.planes.firstOrNull()
                if (plane == null) {
                    closeCapture(); callback?.invoke(null, "capture image had no plane"); callback = null; return@use
                }
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val paddedWidth = rowStride / pixelStride
                val padded = Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888)
                val buffer: ByteBuffer = plane.buffer
                buffer.rewind(); padded.copyPixelsFromBuffer(buffer)
                val bitmap = if (paddedWidth == width) padded else Bitmap.createBitmap(padded, 0, 0, width, height).also { padded.recycle() }
                // The dimensions are the current physical display metrics. The
                // Bitmap is not rotated or cropped; its aspect ratio is preserved
                // as captured and later mapped to the inner panel surface.
                closeCapture()
                callback?.invoke(bitmap, null)
                callback = null
            }
        }, handler)
        val currentProjection = manager.getMediaProjection(resultCode, permissionData)
            ?: run { callback?.invoke(null, "MediaProjection unavailable"); callback = null; closeCapture(); return }
        projection = currentProjection
        display = currentProjection.createVirtualDisplay("FoldPocSnapshot", width, height, density, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, captureReader.surface, null, handler)
    }

    private fun closeCapture() {
        display?.release(); display = null
        reader?.close(); reader = null
        projection?.stop(); projection = null
    }

    override fun close() { callback = null; closeCapture() }
}
