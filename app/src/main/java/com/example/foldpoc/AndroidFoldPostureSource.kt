package com.example.foldpoc

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Public AndroidX WindowManager posture source. WindowManager does not promise a
 * continuous hinge angle, so this source exposes conservative discrete estimates:
 * FLAT -> 1.0, HALF_OPENED -> 0.5, and CLOSED/unknown -> 0.0.
 */
class AndroidFoldPostureSource(private val activity: Activity) : FoldProgressSource {
    private val tracker = WindowInfoTracker.getOrCreate(activity)
    private val handler = Handler(Looper.getMainLooper())
    private var collectionJob: Job? = null
    private var scope: CoroutineScope? = null
    private var value = 1f
    private var target = 1f
    private var running = false
    var postureLabel: String = "UNKNOWN / DISCRETE"
        private set
    var isSupported: Boolean = false
        private set
    var onChanged: (() -> Unit)? = null

    private fun onLayoutInfo(info: WindowLayoutInfo) {
        val feature = info.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
        val next = when {
            feature == null -> { isSupported = false; postureLabel = "UNAVAILABLE"; 1f }
            feature.state == FoldingFeature.State.FLAT -> { isSupported = true; postureLabel = "FLAT (DISCRETE)"; 1f }
            feature.state == FoldingFeature.State.HALF_OPENED -> { isSupported = true; postureLabel = "HALF-OPENED (DISCRETE)"; 0.5f }
            else -> { isSupported = true; postureLabel = "CLOSED (DISCRETE)"; 0f }
        }
        target = next
        onChanged?.invoke()
    }
    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            value += (target - value) * 0.22f
            if (abs(target - value) < 0.002f) value = target
            onChanged?.invoke()
            if (value != target) handler.postDelayed(this, 16L)
        }
    }

    override fun progress(): Float = value.coerceIn(0f, 1f)

    fun start() {
        if (running) return
        running = true
        val postureScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = postureScope
        collectionJob = postureScope.launch {
            tracker.windowLayoutInfo(activity).collect { onLayoutInfo(it) }
        }
        handler.post(tick)
    }

    fun stop() {
        if (!running) return
        running = false
        collectionJob?.cancel(); collectionJob = null
        scope?.coroutineContext?.cancel(); scope = null
        handler.removeCallbacks(tick)
    }
}
