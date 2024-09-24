package it.pagopa.swc.smartpos.idpay.printer

import android.graphics.drawable.Drawable
import it.pagopa.swc.smartpos.app_shared.printer.BasePrintReceipt
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableGravity
import it.pagopa.swc.smartpos.app_shared.second_screen.ImageDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.SecondScreenDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.TextDrawable
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc_smartpos.sharedutils.extensions.dateStringToTimestamp
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.sharedutils.extensions.transactionTimeString
import kotlin.math.roundToInt
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class PrintReceipt(private val mainActivity: MainActivity?) : BasePrintReceipt(mainActivity) {

    fun receiptOkDrawable(model: SaleModel?): Drawable? {
        if (mainActivity == null) return null
        val fontSuperBold = RUiKit.font.titillium_web_bold
        val fontBold = RUiKit.font.titillium_web_semi_bold
        val fontRegular = RUiKit.font.titillium_web_regular
        val codesFont = RUiKit.font.roboto_mono
        val superBigText = 30f
        val bigText = 23f
        val smallText = 20f
        var cnt = when {
            BuildConfig.FLAVOR.contains("pax", true) -> 20
            BuildConfig.FLAVOR.contains("poynt", true) -> 16
            else -> 12
        }
        val textDrawableList = ArrayList<TextDrawable>()
        textDrawableList.addOneTextDrawable(RShared.string.payment_receipt, cnt, bigText, fontSuperBold, DrawableGravity.Center)
        cnt += 3
        cnt = textDrawableList.addElementNotBoldAndBold(
            RShared.string.label_transactionTime to model?.timeStamp?.dateStringToTimestamp()?.transactionTimeString().orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt++
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.initiative_id to model?.initiative?.id.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt++
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.amount_printer to model?.amount?.toAmountFormatted().orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start, true,
            true to DrawableGravity.Start
        )
        cnt++
        textDrawableList.addOneTextDrawable(R.string.id_pay_bonus, cnt, superBigText, fontSuperBold, DrawableGravity.Start)
        textDrawableList.addOneTextDrawable(
            model?.availableSale?.toAmountFormatted().orEmpty(), cnt, superBigText, fontSuperBold, DrawableGravity.End, true,
            true to DrawableGravity.Start
        )
        receiptHeight += 105
        cnt += if (BuildConfig.FLAVOR.contains("androidNative", true)) 4 else 8
        cnt = textDrawableList.addElementsWithRoad(
            Triple(RShared.string.receipt_done_by, RShared.string.pago_pa_spa, RShared.string.pago_pa_road),
            cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
            Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
        )
        val textCutPair = model?.milTransactionId?.chunked(4)?.joinToString("-").orEmpty().cutIfNeeded()
        val labelTransactionId = "${getStringSafely(RShared.string.transaction_id)}:\n${textCutPair.first}"
        textDrawableList.addOneTextDrawable(labelTransactionId, cnt, smallText, codesFont, DrawableGravity.Center)
        cnt += textCutPair.second + 4
        val terminalCode = "${getStringSafely(RShared.string.terminal)}: ${mainActivity.sdkUtils?.getCurrentBusiness()?.value?.terminalId.orEmpty()}"
        textDrawableList.addOneTextDrawable(terminalCode, cnt, smallText, codesFont, DrawableGravity.Center)
        receiptHeight += if (BuildConfig.FLAVOR.contains("pax", true)) 240 else 200
        val imageList = ArrayList<ImageDrawable>()
        val isAndroidNative = BuildConfig.FLAVOR.contains("androidNative", true)
        imageList.addOneImageDrawable(mainActivity, R.drawable.logo_nero, 0, 160, 90, if (isAndroidNative) 180 else null, if (isAndroidNative) 180 else null)
        return SecondScreenDrawable.withTextDrawables(textDrawableList).withImageDrawables(imageList).withBackGroundColor(R.color.white)
            .constructAsLinearLayout(mainActivity, cnt + 8)
    }

    fun receiptNotOkDrawable(model: SaleModel?): Drawable? {
        if (mainActivity == null) return null
        val fontSuperBold = RUiKit.font.titillium_web_bold
        val fontBold = RUiKit.font.titillium_web_semi_bold
        val fontRegular = RUiKit.font.titillium_web_regular
        val codesFont = RUiKit.font.roboto_mono
        val superBigText = 30f
        val bigText = 23f
        val smallText = 20f
        var cnt = when {
            BuildConfig.FLAVOR.contains("pax", true) -> 20
            BuildConfig.FLAVOR.contains("poynt", true) -> 16
            else -> 12
        }
        val textDrawableList = ArrayList<TextDrawable>()
        textDrawableList.addOneTextDrawable(R.string.payment_receipt_cancel, cnt, bigText, fontSuperBold, DrawableGravity.Center)
        cnt += 3
        cnt = textDrawableList.addElementNotBoldAndBold(
            RShared.string.label_transactionTime to model?.timeStamp?.dateStringToTimestamp()?.transactionTimeString().orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt++
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.initiative_id to model?.initiative?.id.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt++
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.amount_printer to model?.amount?.toAmountFormatted().orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start, true,
            true to DrawableGravity.Start
        )
        cnt++
        textDrawableList.addOneTextDrawable(R.string.id_pay_bonus, cnt, superBigText, fontSuperBold, DrawableGravity.Start)
        textDrawableList.addOneTextDrawable(model?.availableSale?.toAmountFormatted().orEmpty(), cnt, superBigText, fontSuperBold, DrawableGravity.End)
        cnt += 3
        textDrawableList.addOneTextDrawable(R.string.payment_receipt_cancel_descr_1, cnt, smallText, fontRegular, DrawableGravity.Start)
        cnt += 2
        textDrawableList.addOneTextDrawable(
            R.string.payment_receipt_cancel_descr_2,
            cnt,
            smallText,
            fontRegular,
            DrawableGravity.Start,
            true,
            true to DrawableGravity.Start
        )
        receiptHeight += 125
        cnt += if (BuildConfig.FLAVOR.contains("androidNative", true)) 4 else 8
        cnt = textDrawableList.addElementsWithRoad(
            Triple(RShared.string.receipt_done_by, RShared.string.pago_pa_spa, RShared.string.pago_pa_road),
            cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
            Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
        )
        val textCutPair = model?.milTransactionId?.chunked(4)?.joinToString("-").orEmpty().cutIfNeeded()
        val labelTransactionId = "${getStringSafely(RShared.string.transaction_id)}:\n${textCutPair.first}"
        textDrawableList.addOneTextDrawable(labelTransactionId, cnt, smallText, codesFont, DrawableGravity.Center)
        cnt += textCutPair.second + 4
        val terminalCode = "${getStringSafely(RShared.string.terminal)}: ${mainActivity.sdkUtils?.getCurrentBusiness()?.value?.terminalId.orEmpty()}"
        textDrawableList.addOneTextDrawable(terminalCode, cnt, smallText, codesFont, DrawableGravity.Center)
        receiptHeight += if (BuildConfig.FLAVOR.contains("pax", true)) 240 else 200
        val imageList = ArrayList<ImageDrawable>()
        val isAndroidNative = BuildConfig.FLAVOR.contains("androidNative", true)
        imageList.addOneImageDrawable(mainActivity, R.drawable.logo_nero, 0, 160, 90, if (isAndroidNative) 180 else null, if (isAndroidNative) 180 else null)
        return SecondScreenDrawable.withTextDrawables(textDrawableList).withImageDrawables(imageList).withBackGroundColor(R.color.white)
            .constructAsLinearLayout(mainActivity, cnt + 8)
    }

    private fun String.cutIfNeeded(): Pair<String, Int> {
        return if (this.length <= 20) this to 0
        else {
            val half = (this.length.toFloat() / 2f).roundToInt()
            this.substring(0, half) + "\n" + this.substring(half, this.length) to 1
        }
    }
}