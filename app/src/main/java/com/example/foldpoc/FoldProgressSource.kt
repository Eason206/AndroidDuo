package com.example.foldpoc

/** Abstraction for the thing that supplies normalized fold progress. */
interface FoldProgressSource { fun progress(): Float }

/** Temporary slider-backed source. A hinge sensor can implement the same interface later. */
class SliderFoldProgressSource : FoldProgressSource {
    private var value = 1f
    fun setProgress(progress: Float) { value = progress.coerceIn(0f, 1f) }
    override fun progress(): Float = value
}
