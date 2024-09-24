package it.pagopa.swc.smartpos.app_shared.second_screen

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape


class ImageDrawable(
    private val originalDrawable: Drawable?,
    private val id: Int,
    private val gravity: DrawableGravity = DrawableGravity.Center,
    private val minWidth: Int = 0,
    private val minHeight: Int = 0,
    private val customGravity: Float? = null,
    private val fixedWidth: Int? = null,
    private val fixedHeight: Int? = null
) {
    private var listSizeLL = 0
    fun build(listSize: Int): ShapeDrawable {
        val shape: Shape = object : Shape() {
            override fun draw(canvas: Canvas, paint: Paint) {
                val multiplier = canvas.height * ((10f / (listSize.toFloat() + 1f)) / 10)
                val y = multiplier * (id.toFloat() + 1f)
                canvas.draw(paint, y, false)
            }
        }
        return ShapeDrawable(shape)
    }

    fun withListSizeLL(size: Int) = apply {
        listSizeLL = size
    }

    private fun Canvas.draw(paint: Paint, y: Float, isLL: Boolean) {
        if (customGravity != null)
            this.drawImageCustomGravity(originalDrawable, paint, y, minWidth, minHeight, customGravity)
        else {
            when (gravity) {
                DrawableGravity.Start -> this.drawImageStart(originalDrawable, paint, y, minWidth, minHeight)
                DrawableGravity.Center -> if (isLL)
                    this.drawImageInCenterForLL(originalDrawable, id, paint, y, minWidth, minHeight)
                else
                    this.drawImageInCenter(originalDrawable, paint, y, minWidth, minHeight)

                else -> this.drawImageEnd(originalDrawable, paint, y, minWidth, minHeight)
            }
        }
    }

    fun buildAsLinearLayout(): ShapeDrawable {
        val shape: Shape = object : Shape() {
            override fun draw(canvas: Canvas, paint: Paint) {
                val y = canvas.height * (id.toFloat() / listSizeLL.toFloat())
                canvas.draw(paint, y, true)
            }
        }
        return ShapeDrawable(shape)
    }

    private fun Canvas.drawImageInCenter(drawable: Drawable?, paint: Paint, y: Float, minWidth: Int, minHeight: Int) {
        drawable.toBitmap(minWidth, minHeight)?.let { bitmap ->
            val drawableWidth = bitmap.width
            val drawableHeight = bitmap.height
            val cWidth: Int = this.width
            val x: Float = (cWidth / 2f - drawableWidth / 2f)
            this.drawBitmap(bitmap, x, y - drawableHeight / 2f, paint)
        }
    }

    private fun Canvas.drawImageInCenterForLL(drawable: Drawable?, id: Int, paint: Paint, y: Float, minWidth: Int, minHeight: Int) {
        drawable.toBitmap(minWidth, minHeight)?.let { bitmap ->
            val drawableWidth = bitmap.width
            val cWidth: Int = this.width
            val x: Float = (cWidth / 2f - drawableWidth / 2f)
            this.drawBitmap(bitmap, x, if (id == 0) y + 10 else y, paint)
        }
    }

    private fun Canvas.drawImageCustomGravity(drawable: Drawable?, paint: Paint, y: Float, minWidth: Int, minHeight: Int, gravity: Float) {
        drawable.toBitmap(minWidth, minHeight)?.let { bitmap ->
            val drawableHeight = bitmap.height
            val cWidth: Float = this.width.toFloat()
            val x: Float = cWidth * gravity
            this.drawBitmap(bitmap, x, y - drawableHeight / 2f, paint)
        }
    }

    private fun Canvas.drawImageStart(drawable: Drawable?, paint: Paint, y: Float, minWidth: Int, minHeight: Int) {
        drawable.toBitmap(minWidth, minHeight)?.let { bitmap ->
            val drawableHeight = bitmap.height
            val cWidth: Float = this.width.toFloat()
            val x: Float = cWidth * DrawableGravity.Start.value
            this.drawBitmap(bitmap, x, y - drawableHeight / 2f, paint)
        }
    }

    private fun Canvas.drawImageEnd(drawable: Drawable?, paint: Paint, y: Float, minWidth: Int, minHeight: Int) {
        drawable.toBitmap(minWidth, minHeight)?.let { bitmap ->
            val drawableHeight = bitmap.height
            val cWidth: Float = this.width.toFloat()
            val x: Float = (cWidth * DrawableGravity.End.value) - bitmap.width
            this.drawBitmap(bitmap, x, y - drawableHeight / 2f, paint)
        }
    }

    private fun Drawable?.toBitmap(minWidth: Int, minHeight: Int): Bitmap? {
        if (this == null) return null
        if (this is BitmapDrawable && this.bitmap != null) {
            return if (minWidth != 0 && minHeight != 0)
                Bitmap.createScaledBitmap(this.bitmap, minWidth, minHeight, true)
            else
                this.bitmap
        }
        val bitmap: Bitmap = if (this.intrinsicWidth <= 0 || this.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)
        return when {
            fixedWidth != null && fixedHeight != null -> Bitmap.createScaledBitmap(bitmap, fixedWidth, fixedHeight, true)
            else -> if (minWidth != 0 && canvas.width < minWidth && minHeight != 0 && canvas.height < minHeight)
                Bitmap.createScaledBitmap(bitmap, minWidth, minHeight, true)
            else
                bitmap
        }
    }
}