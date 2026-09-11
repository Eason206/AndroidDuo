package com.example.foldpoc

import kotlin.math.cos
import kotlin.math.sin

data class ProjectedPanel(val x: FloatArray, val y: FloatArray, val depth: Float)

/**
 * Two-panel book model in local coordinates.
 * X runs horizontally away from the hinge, Y runs vertically, and Z is depth toward
 * the camera. The hinge is always (0, 0, 0). Progress maps linearly to the physical
 * included angle: 0 = 0 degrees (closed), 1 = 180 degrees (flat/unfolded).
 */
object FoldGeometry {
    fun foldAngle(progress: Float): Float = (progress.coerceIn(0f, 1f) * 180f)

    fun project(progress: Float, panelWidth: Float, panelHeight: Float, camera: Float): List<ProjectedPanel> {
        // Each panel rotates half of the included angle away from the flat position.
        // At 90 degrees included angle, both outer edges have moved in depth while
        // remaining attached to the same hinge.
        val theta = (1f - progress.coerceIn(0f, 1f)) * (Math.PI.toFloat() / 2f)
        val c = cos(theta); val s = sin(theta)
        // A tiny hinge barrel becomes visible only while closing. It disappears at
        // 180 degrees so the two display surfaces meet without a visible gap.
        val hingeSeparation = (1f - c) * 3.5f
        fun panel(side: Float): ProjectedPanel {
            val hingeX = side * hingeSeparation / 2f
            val xs = floatArrayOf(hingeX, hingeX + side * panelWidth * c, hingeX + side * panelWidth * c, hingeX)
            val ys = floatArrayOf(-panelHeight / 2f, -panelHeight / 2f, panelHeight / 2f, panelHeight / 2f)
            val depth = panelWidth * s
            // Simple perspective camera: points farther in +Z shrink toward the
            // projection center. This is intentionally a small, stable FOV.
            val projectedX = FloatArray(4) { i -> xs[i] * camera / (camera + depth) }
            val projectedY = FloatArray(4) { i -> ys[i] * camera / (camera + depth) }
            return ProjectedPanel(projectedX, projectedY, depth)
        }
        return listOf(panel(-1f), panel(1f))
    }
}
