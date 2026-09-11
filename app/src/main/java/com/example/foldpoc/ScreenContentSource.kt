package com.example.foldpoc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

class DemoScreenContentSource(context: Context) : AutoCloseable {
    private val prefs = context.getSharedPreferences("fold_demo", Context.MODE_PRIVATE)
    private var inner = load(context, prefs.getString("inner_uri", null), R.drawable.default_inner)
    private var outer = load(context, prefs.getString("outer_uri", null), R.drawable.default_outer)
    fun innerBitmap(): Bitmap = inner
    fun outerBitmap(): Bitmap = outer
    fun replaceInner(bitmap: Bitmap) { if (bitmap !== inner) { inner.recycle(); inner = bitmap } }
    fun replaceOuter(bitmap: Bitmap) { if (bitmap !== outer) { outer.recycle(); outer = bitmap } }
    fun loadUri(context: Context, uri: Uri, isInner: Boolean): Boolean = try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return false
        if (isInner) { replaceInner(bitmap); prefs.edit().putString("inner_uri", uri.toString()).apply() }
        else { replaceOuter(bitmap); prefs.edit().putString("outer_uri", uri.toString()).apply() }
        true
    } catch (_: Exception) { false }
    private fun load(context: Context, value: String?, fallback: Int): Bitmap = try {
        if (value != null) context.contentResolver.openInputStream(Uri.parse(value))?.use { BitmapFactory.decodeStream(it) } ?: BitmapFactory.decodeResource(context.resources, fallback)
        else BitmapFactory.decodeResource(context.resources, fallback)
    } catch (_: Exception) { BitmapFactory.decodeResource(context.resources, fallback) }
    override fun close() { if (!inner.isRecycled) inner.recycle(); if (!outer.isRecycled) outer.recycle() }
}
