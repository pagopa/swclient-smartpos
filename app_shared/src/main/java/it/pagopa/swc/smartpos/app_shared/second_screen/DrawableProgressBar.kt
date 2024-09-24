package it.pagopa.swc.smartpos.app_shared.second_screen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.os.Build
import androidx.annotation.ColorRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity
import it.pagopa.swc_smartpos.ui_kit.utils.fromColorResToHexadecimalString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class DrawableProgressBar private constructor() {
    private var context: Context? = null
    private var toCancelJob = false
    private val oval = RectF()
    private var progressWidth: Float = 0f
    private var backgroundWidth: Float = 20f
    private var progressColor by Delegates.notNull<Int>()
    private var backgroundColor by Delegates.notNull<Int>()
    private var wholeBackgroundColor by Delegates.notNull<Int>()
    var progress: Float = 0f
    private var mTextDrawable = MutableLiveData<TextDrawable?>(null)
    private val observer = Observer<TextDrawable?> {
        (context.findActivity() as? BaseMainActivity<*>)?.let { mainActivity ->
            val listDrawables = ArrayList<Drawable>()
            val color = Color.parseColor(wholeBackgroundColor.fromColorResToHexadecimalString(mainActivity))
            listDrawables.add(GradientDrawable().apply {
                colors = intArrayOf(color, color)
            })
            listDrawables.add(this@DrawableProgressBar.build(mainActivity))
            it?.let { txtDrawable->
                listDrawables.add(txtDrawable.build(mainActivity, 1))
                mainActivity.sdkUtils?.displayDrawable(LayerDrawable(listDrawables.toTypedArray()).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        setLayerInsetBottom(1, 400)
                        setLayerInsetStart(1, 550)
                        setLayerInsetEnd(1, 550)
                    }
                })
            }
        }
    }

    private fun build(context: Context): Drawable {
        val progressPaint = Paint().apply {
            color = Color.parseColor(progressColor.fromColorResToHexadecimalString(context))
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }
        val backgroundPaint = Paint().apply {
            color = Color.parseColor(backgroundColor.fromColorResToHexadecimalString(context))
            style = Paint.Style.STROKE
            strokeWidth = backgroundWidth
            isAntiAlias = true
        }

        var centerX = 0f
        var centerY = 0f
        var radius = 0f
        val shape: Shape = object : Shape() {
            override fun onResize(width: Float, height: Float) {
                centerX = width / 2
                centerY = height / 2
                radius = width / 2 - progressWidth
                oval.set(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                )
                super.onResize(width, height)
            }

            override fun draw(canvas: Canvas, paint: Paint) {
                canvas.drawCircle(centerX, centerY, radius, backgroundPaint)
                canvas.drawArc(oval, 270f, 360f * progress, false, progressPaint)
            }
        }
        return ShapeDrawable(shape)
    }

    fun launch() {
        toCancelJob = false
        CoroutineScope(Dispatchers.Main).launch {
            while (!toCancelJob) {
                delay(100L)
                if (progress < 1f)
                    progress += 0.07f
                else progress = 0f
                mTextDrawable.postValue(mTextDrawable.value)
            }
        }
    }

    fun displayWithText(context: Context, textDrawable: TextDrawable) {
        this.context = context
        mTextDrawable.observeForever(observer)
        mTextDrawable.postValue(textDrawable)
    }

    fun cancelJob() {
        mTextDrawable.removeObserver(observer)
        mTextDrawable.postValue(null)
        toCancelJob = true
    }

    companion object {
        fun construct(
            progressWidth: Float,
            backgroundWidth: Float,
            @ColorRes progressColor: Int,
            @ColorRes backgroundColor: Int,
            wholeBackgroundColor: Int
        ) = DrawableProgressBar().apply {
            this.progressWidth = progressWidth
            this.backgroundWidth = backgroundWidth
            this.progressColor = progressColor
            this.backgroundColor = backgroundColor
            this.wholeBackgroundColor = wholeBackgroundColor
        }
    }
}