package it.pagopa.swc.smartpos.app_shared.second_screen

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import it.pagopa.swc_smartpos.ui_kit.utils.fromColorResToHexadecimalString

data class TextDrawable(
    private val text: String,
    @ColorRes private val textColor: Int,
    private val id: Int,
    private val textSize: Float = 32f,
    @FontRes private val textFont: Int = it.pagopa.swc_smart_pos.ui_kit.R.font.readex_pro,
    private val gravity: DrawableGravity = DrawableGravity.Center,
    private val underLine: Line? = null,
    private val customGravity: Float? = null
) {
    private var listSizeLL = 0

    data class Line(
        @ColorRes val color: Int,
        val heightPx: Int,
        val distancePx: Int,
        val alignToTextGravity: Pair<Boolean, DrawableGravity> = false to DrawableGravity.Start
    ) {
        fun build(context: Context, canvas: Canvas, y: Float) {
            val (align, gravity) = alignToTextGravity
            val x = if (align) canvas.width.toFloat() * gravity.value else 0f
            val lineColor = Color.parseColor(color.fromColorResToHexadecimalString(context))
            val startY = y + distancePx.toFloat()
            canvas.drawLine(x, startY,
                canvas.width.toFloat() - x,
                startY, Paint().apply {
                    this.strokeWidth = heightPx.toFloat()
                    this.color = lineColor
                })
        }
    }

    fun build(context: Context, listSize: Int): ShapeDrawable {
        val shape: Shape = object : Shape() {
            override fun draw(canvas: Canvas, paint: Paint) {
                paint.construct(context)
                val multiplier = canvas.height * ((10f / (listSize.toFloat() + 1f)) / 10)
                val y = multiplier * (id.toFloat() + 1f)
                val x = canvas.width.toFloat() * gravity.value
                val finalTextArray = text.split("\n")
                finalTextArray.forEachIndexed { index, it ->
                    val myY = y + (index * 70)
                    if (customGravity != null)
                        canvas.drawText(it, canvas.width.toFloat() * customGravity, myY, paint)
                    else {
                        when (gravity) {
                            DrawableGravity.Center -> canvas.drawTextInCenter(paint, it, myY)
                            DrawableGravity.End -> canvas.drawTextInEnd(paint, it, myY, gravity.value)
                            else -> canvas.drawText(it, x, myY, paint)
                        }
                    }
                }
                underLine?.build(context, canvas, y)
            }
        }
        return ShapeDrawable(shape)
    }

    private fun Paint.construct(context: Context) {
        val color = Color.parseColor(textColor.fromColorResToHexadecimalString(context))
        val font = ResourcesCompat.getFont(context, textFont)
        this.color = color
        this.textSize = this@TextDrawable.textSize
        this.typeface = font
    }

    fun buildAsLinearLayout(context: Context, howMuchWhenIndent: Int = 20): ShapeDrawable {
        val shape: Shape = object : Shape() {
            override fun draw(canvas: Canvas, paint: Paint) {
                paint.construct(context)
                val y = canvas.height * (id.toFloat() / listSizeLL.toFloat())
                val x = canvas.width.toFloat() * gravity.value
                val finalTextArray = text.split("\n")
                finalTextArray.forEachIndexed { index, it ->
                    val myY = y + (index * howMuchWhenIndent)
                    if (customGravity != null)
                        canvas.drawText(it, canvas.width.toFloat() * customGravity, myY, paint)
                    else {
                        when (gravity) {
                            DrawableGravity.Center -> canvas.drawTextInCenter(paint, it, myY)
                            DrawableGravity.End -> canvas.drawTextInEnd(paint, it, myY, gravity.value)
                            else -> canvas.drawText(it, x, myY, paint)
                        }
                    }
                }
                underLine?.build(context, canvas, y)
            }
        }
        return ShapeDrawable(shape)
    }

    fun withListSizeLL(size: Int) = apply {
        listSizeLL = size
    }

    private fun Canvas.drawTextInCenter(paint: Paint, text: String, y: Float) {
        val r = Rect()
        this.getClipBounds(r)
        val cWidth: Int = r.width()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, r)
        val x: Float = cWidth / 2f - r.width() / 2f - r.left
        this.drawText(text, x, y, paint)
    }

    private fun Canvas.drawTextInEnd(paint: Paint, text: String, y: Float, gravityValue: Float) {
        val r = Rect()
        this.getClipBounds(r)
        val cWidth: Int = r.width()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, r)
        val toDelete = cWidth - (cWidth * gravityValue)
        val x: Float = (cWidth - r.width()).toFloat() - toDelete
        this.drawText(text, x, y, paint)
    }
}