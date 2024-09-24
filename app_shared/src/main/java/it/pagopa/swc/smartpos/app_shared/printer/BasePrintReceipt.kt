package it.pagopa.swc.smartpos.app_shared.printer

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.BuildConfig
import it.pagopa.swc.smartpos.app_shared.R
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableGravity
import it.pagopa.swc.smartpos.app_shared.second_screen.ImageDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.TextDrawable

abstract class BasePrintReceipt(private val mainActivity: BaseMainActivity<*>?) {
    var receiptHeight = 350
    private val lineDistancePx = 27
    fun getStringSafely(id: Int) = mainActivity?.resources?.getString(id).orEmpty()

    fun ArrayList<TextDrawable>.addOneTextDrawable(
        text: Any,
        count: Int,
        textSize: Float,
        @FontRes font: Int,
        gravity: DrawableGravity,
        isUnderLine: Boolean = false,
        alignLineToTextGravity: Pair<Boolean, DrawableGravity> = false to DrawableGravity.Start
    ) {
        val lineColor = if (BuildConfig.FLAVOR.contains("androidNative", true))
            R.color.grey_light
        else
            R.color.blue_grey_dark
        val mText = if (text is Int) getStringSafely(text) else (text as? String).orEmpty()
        this.add(
            TextDrawable(
                mText, R.color.real_black, count, textSize, font, gravity, if (isUnderLine) {
                    TextDrawable.Line(lineColor, 2, lineDistancePx, alignLineToTextGravity)
                } else {
                    null
                }
            )
        )
    }

    fun ArrayList<TextDrawable>.addElementNotBoldAndBold(
        texts: Pair<Int, Any>,
        count: Int,
        textSize: Float,
        fonts: Pair<Int, Int>,
        gravities: Pair<DrawableGravity, DrawableGravity>,
        underLine: Boolean = false,
        alignLineToTextGravity: Pair<Boolean, DrawableGravity> = false to DrawableGravity.Start
    ): Int {
        var cnt = count
        this.addOneTextDrawable(texts.first, cnt, textSize, fonts.first, gravities.first)
        cnt += 2
        this.addOneTextDrawable(texts.second, cnt, textSize, fonts.second, gravities.second, underLine, alignLineToTextGravity)
        receiptHeight += 70
        if (underLine)
            receiptHeight += 20
        return cnt + if (underLine) 3 else 2
    }

    fun ArrayList<ImageDrawable>.addOneImageDrawable(
        context: Context?,
        @DrawableRes drawable: Int,
        id: Int,
        width: Int,
        height: Int,
        fixedWidth: Int? = null,
        fixedHeight: Int? = null
    ) {
        if (context == null) return
        this.add(ImageDrawable(ContextCompat.getDrawable(context, drawable), id, DrawableGravity.Center, width, height, null, fixedWidth, fixedHeight))
    }

    fun ArrayList<TextDrawable>.addElementsWithRoad(
        texts: Triple<Any, Any, Any>,
        count: Int,
        textSize: Triple<Float, Float, Float>,
        fonts: Triple<Int, Int, Int>,
        gravities: Triple<DrawableGravity, DrawableGravity, DrawableGravity>,
        underLine: Boolean = false
    ): Int {
        var cnt = count
        this.addOneTextDrawable(texts.first, cnt, textSize.first, fonts.first, gravities.first)
        cnt += 3
        this.addOneTextDrawable(texts.second, cnt, textSize.second, fonts.second, gravities.second)
        cnt += 2
        this.addOneTextDrawable(texts.third, cnt, textSize.third, fonts.third, gravities.third, underLine)
        receiptHeight += 150
        return cnt + if (underLine) 4 else 3
    }
}