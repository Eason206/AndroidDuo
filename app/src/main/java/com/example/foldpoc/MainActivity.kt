package com.example.foldpoc

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    private lateinit var content: DemoScreenContentSource
    private lateinit var demo: FoldAnimationView
    private lateinit var settings: LinearLayout
    private lateinit var posture: AndroidFoldPostureSource
    private lateinit var slab: SlabTiltSource
    private val outerPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { it?.let { load(it, false) } }
    private val innerPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { it?.let { load(it, true) } }

    override fun onCreate(state: Bundle?) { super.onCreate(state)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        content = DemoScreenContentSource(this)
        posture = AndroidFoldPostureSource(this); slab = SlabTiltSource(this)
        demo = FoldAnimationView(this, content, posture, slab)
        val root = FrameLayout(this); root.addView(demo, FrameLayout.LayoutParams(-1, -1))
        settings = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(36, 48, 36, 36); setBackgroundColor(Color.argb(238, 12, 14, 22)); visibility = View.GONE }
        settings.addView(TextView(this).apply { text = "Fold Animation Settings"; textSize = 22f; setTextColor(Color.WHITE); setPadding(0, 0, 0, 24) })
        settings.addView(action("选择外屏截图") { outerPicker.launch("image/*") })
        settings.addView(action("选择内屏截图") { innerPicker.launch("image/*") })
        settings.addView(action("开启测试") { settings.visibility = View.GONE; demo.reset() })
        root.addView(settings, FrameLayout.LayoutParams(-1, -2, Gravity.TOP)); setContentView(root)
        posture.onChanged = { demo.onPostureChanged() }; slab.onChanged = { demo.invalidate() }
    }
    private fun action(text: String, click: () -> Unit) = Button(this).apply { this.text = text; setOnClickListener { click() } }
    private fun load(uri: Uri, inner: Boolean) { if (!content.loadUri(this, uri, inner)) Toast.makeText(this, "图片无法读取，继续使用当前图片", Toast.LENGTH_SHORT).show(); demo.invalidate() }
    fun openSettings() { settings.visibility = View.VISIBLE }
    override fun onStart() { super.onStart(); posture.start(); slab.start() }
    override fun onStop() { posture.stop(); slab.stop(); super.onStop() }
    override fun onDestroy() { posture.stop(); slab.close(); content.close(); super.onDestroy() }
}

class FoldAnimationView(context: android.content.Context, private val content: DemoScreenContentSource, private val posture: AndroidFoldPostureSource, private val slab: SlabTiltSource) : View(context) {
    private val renderer = FoldRenderer(content); private var mode = DeviceAnimationMode.SLAB; private var progress = 0f; private var manualTilt = 0f; private var lastX = 0f; private var dragging = false; private var longPress: Runnable? = null; private var settling = false
    override fun onDraw(canvas: android.graphics.Canvas) { renderer.draw(canvas, mode, if(mode==DeviceAnimationMode.FOLDABLE) posture.progress() else progress, if(slab.available) slab.relativeDegrees() else manualTilt, width.toFloat(), height.toFloat()); if (settling) postInvalidateOnAnimation() }
    fun onPostureChanged() { mode = if (posture.isSupported) DeviceAnimationMode.FOLDABLE else DeviceAnimationMode.SLAB; invalidate() }
    fun reset() { progress = 0f; manualTilt = 0f; settling = false; invalidate() }
    override fun onTouchEvent(e: MotionEvent): Boolean = when (e.actionMasked) {
        MotionEvent.ACTION_DOWN -> { lastX = e.x; dragging = false; longPress = Runnable { if (!dragging) (context as MainActivity).openSettings() }; postDelayed(longPress!!, 600); true }
        MotionEvent.ACTION_MOVE -> { if (abs(e.x - lastX) > 10f) { dragging = true; longPress?.let { removeCallbacks(it) }; if(mode==DeviceAnimationMode.FOLDABLE) progress=(progress+(e.x-lastX)/width).coerceIn(0f,1f) else manualTilt=(manualTilt+(e.x-lastX)/width*30f).coerceIn(-30f,30f); lastX=e.x; invalidate() }; true }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { longPress?.let { removeCallbacks(it) }; if (dragging && mode==DeviceAnimationMode.FOLDABLE) settle(if (progress < .5f) 0f else 1f); true }
        else -> true
    }
    private fun settle(target: Float) { settling = true; progress += (target - progress) * .16f; if (abs(target - progress) < .004f) { progress = target; settling = false }; postInvalidateOnAnimation(); if (settling) postOnAnimation { settle(target) } }
}
