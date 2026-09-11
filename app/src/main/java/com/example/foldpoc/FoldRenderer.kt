package com.example.foldpoc

import android.graphics.*
import android.os.Build
import android.util.Log
import kotlin.math.abs

class FoldRenderer(private val content: DemoScreenContentSource) {
    private val gpu = if (Build.VERSION.SDK_INT >= 33) GpuReprojectionRenderer() else null
    private val fallback = MeshFallbackRenderer(content)
    fun draw(canvas: Canvas, mode: DeviceAnimationMode, progress: Float, tiltDegrees: Float, width: Float, height: Float) {
        canvas.drawColor(Color.BLACK)
        if (gpu != null) {
            val drawn = if (mode == DeviceAnimationMode.FOLDABLE) gpu.drawFoldable(canvas, content, progress, width, height)
            else gpu.drawSlab(canvas, content, tiltDegrees, width, height)
            if (!drawn) {
                if (mode == DeviceAnimationMode.FOLDABLE) fallback.drawFoldable(canvas, progress, width, height)
                else fallback.drawSlab(canvas, tiltDegrees, width, height)
            }
        } else if (mode == DeviceAnimationMode.FOLDABLE) fallback.drawFoldable(canvas, progress, width, height)
        else fallback.drawSlab(canvas, tiltDegrees, width, height)
    }
}

/** Complete source plane -> rotated glass -> eye ray -> fixed UI plane. */
private class GpuReprojectionRenderer {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var shader: RuntimeShader? = null
    private var initializationError: Throwable? = null
    init {
        try { shader = RuntimeShader(AGSL) }
        catch (error: RuntimeException) {
            initializationError = error
            Log.e("FoldRenderer", "AGSL shader compilation failed; using mesh fallback", error)
        }
    }
    fun drawSlab(canvas: Canvas, content: DemoScreenContentSource, tilt: Float, w: Float, h: Float): Boolean {
        val angle = tilt.coerceIn(-30f, 30f) / 30f * 1.05f
        return draw(canvas, content.innerBitmap(), angle, w, h, 0.12f, 0.015f, 1f)
    }
    fun drawFoldable(canvas: Canvas, content: DemoScreenContentSource, progress: Float, w: Float, h: Float): Boolean {
        val p = progress.coerceIn(0f, 1f)
        val outer = draw(canvas, content.outerBitmap(), p * .95f, w, h, .16f, .018f, 1f - smooth(.72f, 1f, p))
        val inner = draw(canvas, content.innerBitmap(), -p * .95f, w, h, .16f, .018f, smooth(.08f, .90f, p))
        return outer && inner
    }
    private fun draw(canvas: Canvas, bitmap: Bitmap, angle: Float, w: Float, h: Float, spread: Float, darkening: Float, alpha: Float): Boolean {
        val activeShader = shader ?: return false
        val child = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        try {
            activeShader.setInputShader("content", child)
            activeShader.setFloatUniform("size", w, h)
            activeShader.setFloatUniform("sourceSize", bitmap.width.toFloat(), bitmap.height.toFloat())
            activeShader.setFloatUniform("angle", angle)
            activeShader.setFloatUniform("eyeDistance", h * 1.8f)
            activeShader.setFloatUniform("blurSpread", spread)
            activeShader.setFloatUniform("darkening", darkening)
        } catch (error: RuntimeException) {
            Log.e("FoldRenderer", "AGSL uniform/input setup failed; using mesh fallback", error)
            shader = null
            return false
        }
        paint.shader = activeShader; paint.alpha = (alpha * 255f).toInt().coerceIn(0, 255)
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null; paint.alpha = 255
        return true
    }
    private fun smooth(a: Float, b: Float, x: Float): Float { val q=((x-a)/(b-a)).coerceIn(0f,1f); return q*q*(3f-2f*q) }
    private companion object {
        val AGSL = """
            uniform shader content;
            uniform float2 size;
            uniform float2 sourceSize;
            uniform float angle;
            uniform float eyeDistance;
            uniform float blurSpread;
            uniform float darkening;
            float hash21(float2 p) { return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453); }
            half4 source(float2 ui) {
                float scale = min(size.x / sourceSize.x, size.y / sourceSize.y);
                float2 offset = (size - sourceSize * scale) * .5;
                float2 uv = (ui - offset) / scale;
                if (uv.x < 0.0 || uv.y < 0.0 || uv.x > sourceSize.x || uv.y > sourceSize.y) return half4(0.0);
                return content.eval(uv);
            }
            half4 main(float2 p) {
                float tilt = abs(angle);
                if (tilt < .0001) return source(p);
                bool fixedRight = angle < 0.0;
                float hinge = fixedRight ? size.x : 0.0;
                float side = fixedRight ? -1.0 : 1.0;
                float d = abs(p.x - hinge);
                float3 glass = float3(hinge + side * d * cos(tilt), p.y, d * sin(tilt));
                float3 eye = float3(size.x*.5, size.y*.5, eyeDistance);
                float depth = eye.z - glass.z;
                if (depth <= .001) return half4(0.0);
                float t = eye.z / depth;
                float2 hit = eye.xy + (glass.xy - eye.xy) * t;
                float radius = blurSpread * glass.z;
                if (hit.x < -radius || hit.y < -radius || hit.x > size.x + radius || hit.y > size.y + radius) return half4(0.0);
                half attenuation = half(max(1.0 - darkening * radius, 0.0));
                if (radius < .5) return source(hit) * attenuation;
                half3 sum = half3(0.0); float rotation = hash21(p) * 6.2831853;
                for (int i=0; i<16; i++) { float q=(float(i)+.5)/16.0; float r=radius*sqrt(q); float a=float(i)*2.3999632+rotation; sum += source(hit+r*float2(cos(a),sin(a))).rgb; }
                return half4(sum/16.0*attenuation, 1.0);
            }
        """.trimIndent()
    }
}

private class MeshFallbackRenderer(private val content: DemoScreenContentSource) {
    private val n=16; private val mesh=FloatArray((n+1)*(n+1)*2); private val paint=Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    fun drawSlab(c:Canvas, tilt:Float,w:Float,h:Float){c.drawColor(Color.BLACK);draw(c,content.innerBitmap(),tilt.coerceIn(-30f,30f)/30f*1.05f,w,h)}
    fun drawFoldable(c:Canvas,p:Float,w:Float,h:Float){c.drawColor(Color.BLACK);draw(c,content.outerBitmap(),p*.8f,w,h);paint.alpha=(smooth(.08f,.9f,p)*255).toInt();draw(c,content.innerBitmap(),-p*.8f,w,h);paint.alpha=255}
    private fun draw(c:Canvas,b:Bitmap,a:Float,w:Float,h:Float){val fixedRight=a<0;val hinge=if(fixedRight)w else 0f;val side=if(fixedRight)-1f else 1f;val s=kotlin.math.sin(abs(a));val co=kotlin.math.cos(abs(a));var k=0;for(y in 0..n)for(x in 0..n){val u=x.toFloat()/n;val v=y.toFloat()/n;val d=abs(u*w-hinge);val q=1f/(1f+d*s/(h*1.8f));mesh[k++]=hinge+side*d*co*q;mesh[k++]=v*h*q+(1f-q)*h*.5f};c.drawBitmapMesh(b,n,n,mesh,0,null,0,paint)}
    private fun smooth(a:Float,b:Float,x:Float):Float{val q=((x-a)/(b-a)).coerceIn(0f,1f);return q*q*(3f-2f*q)}
}
