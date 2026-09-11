package com.example.foldpoc

import android.graphics.Bitmap

/** View of a source-owned bitmap; replacing it does not recreate the renderer. */
class ScreenSurface(private var current: Bitmap) {
    // drawBitmapMesh maps the complete source image to the complete physical
    // panel. A capture provider should therefore publish a bitmap with the
    // display's native aspect ratio; differing inner/outer resolutions are fine.
    // No crop or per-frame resampling is performed here.
    fun update(bitmap: Bitmap) { current = bitmap }
    fun bitmap(): Bitmap = current
}
