package it.pagopa.swc_smartpos.printer

import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import it.pagopa.swc.smartpos.app_shared.printer.BasePrintReceipt
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableGravity
import it.pagopa.swc.smartpos.app_shared.second_screen.ImageDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.SecondScreenDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.TextDrawable
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.sharedutils.qrCode.generate_qr.QrCodeUtils
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel
import kotlin.math.roundToInt
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class PrintReceipt(private val mainActivity: MainActivity?):BasePrintReceipt(mainActivity) {
    fun printReceipt(model: ReceiptModel?, status: Status? = null): Drawable? {
        if (model == null) return null
        return if (model.state == BaseResultFragment.State.Success)
            receiptOkDrawable(model)
        else
            receiptNotOkDrawable(model, status)
    }

    private fun receiptOkDrawable(model: ReceiptModel): Drawable? {
        if (mainActivity == null) return null
        val fontSuperBold = RUiKit.font.titillium_web_bold
        val fontBold = RUiKit.font.titillium_web_semi_bold
        val fontRegular = RUiKit.font.titillium_web_regular
        val superBigText = 30f
        val bigText = 23f
        val smallText = 20f
        var cnt = 6
        val textDrawableList = ArrayList<TextDrawable>()
        textDrawableList.addOneTextDrawable(RShared.string.payment_receipt, cnt, bigText, fontSuperBold, DrawableGravity.Center)
        cnt += 3
        cnt = textDrawableList.addElementNotBoldAndBold(
            RShared.string.label_transactionTime to model.labelDateAndTime.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start, true
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_payee to model.labelPayee.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_payeeTaxCode to model.labelPayeeTaxCode.orEmpty(),
            cnt, smallText, fontRegular to RUiKit.font.roboto_bold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_noticeCode to model.labelNoticeCode.orEmpty(),
            cnt, smallText, fontRegular to RUiKit.font.roboto_bold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_paymentReason to model.labelPaymentReason.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_Amount to model.labelAmount.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start, true
        )
        textDrawableList.addOneTextDrawable(R.string.label_fee, cnt, smallText, fontRegular, DrawableGravity.Start)
        textDrawableList.addOneTextDrawable(model.labelFee.orEmpty(), cnt, smallText, fontBold, DrawableGravity.End)
        receiptHeight += 45
        cnt += 2
        textDrawableList.addOneTextDrawable(R.string.label_toBePaid, cnt, superBigText, fontBold, DrawableGravity.Start)
        textDrawableList.addOneTextDrawable(model.labelTotalAmount.orEmpty(), cnt, superBigText, fontBold, DrawableGravity.End, true)
        receiptHeight += 105
        cnt += 4
        cnt = textDrawableList.addElementsWithRoad(
            Triple(R.string.payment_managed_by, "Nexi Payments S.p.A.", "Corso Sempione, 55 · 20149, Milano"),
            cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
            Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
        )
        cnt = textDrawableList.addElementsWithRoad(
            Triple(RShared.string.receipt_done_by, RShared.string.pago_pa_spa, RShared.string.pago_pa_road),
            cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
            Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
        )
        val textCutPair = model.transactionID?.chunked(4)?.joinToString("-").orEmpty().cutIfNeeded()
        val labelTransactionId = "${getStringSafely(R.string.transaction_id)}:\n${textCutPair.first}"
        textDrawableList.addOneTextDrawable(labelTransactionId, cnt, smallText, RUiKit.font.roboto_light, DrawableGravity.Center)
        cnt += textCutPair.second + 3
        val terminalCode = "${getStringSafely(R.string.terminal)}: ${model.labelTerminalCode.orEmpty()}"
        textDrawableList.addOneTextDrawable(terminalCode, cnt, smallText, RUiKit.font.roboto_light, DrawableGravity.Center)
        receiptHeight += 150
        val imageList = ArrayList<ImageDrawable>()
        imageList.addOneImageDrawable(mainActivity, RUiKit.drawable.logo_nero, 0, 160, 90)
        return SecondScreenDrawable.withTextDrawables(textDrawableList).withImageDrawables(imageList).withBackGroundColor(R.color.white)
            .constructAsLinearLayout(mainActivity, cnt + 8)
    }

    private fun receiptNotOkDrawable(model: ReceiptModel, status: Status? = null): Drawable? {
        if (mainActivity == null) return null
        val fontSuperBold = RUiKit.font.titillium_web_bold
        val fontBold = RUiKit.font.titillium_web_semi_bold
        val fontRegular = RUiKit.font.titillium_web_regular
        val bigText = 23f
        val smallText = 20f
        var cnt = 7
        val textDrawableList = ArrayList<TextDrawable>()
        textDrawableList.addOneTextDrawable(R.string.assistance_contact, cnt, bigText, fontSuperBold, DrawableGravity.Center)
        cnt += 3
        textDrawableList.addOneTextDrawable(R.string.receipt_not_ok_description, cnt, bigText, fontBold, DrawableGravity.Center)
        cnt += 17
        val mail = getStringSafely(R.string.pagoPa_mail)
        textDrawableList.addOneTextDrawable(mail, cnt, bigText, fontBold, DrawableGravity.Center, true)
        cnt += 4
        cnt = textDrawableList.addElementNotBoldAndBold(
            RShared.string.label_transactionTime to model.labelDateAndTime.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        val textCutPair = model.transactionID?.chunked(4)?.joinToString("-").orEmpty().cutIfNeeded()
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.transaction_id to textCutPair.first,
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt += textCutPair.second
        cnt = textDrawableList.addElementNotBoldAndBold(
            (if (status == Status.ERROR_ON_CLOSE || status == Status.ERROR_ON_RESULT) R.string.label_Amount_to_refund else R.string.label_Amount) to model.labelTotalAmount.orEmpty(),
            cnt, smallText, fontRegular to fontBold, DrawableGravity.Start to DrawableGravity.Start, true
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_noticeCode to model.labelNoticeCode.orEmpty(),
            cnt, smallText, fontRegular to RUiKit.font.roboto_bold, DrawableGravity.Start to DrawableGravity.Start
        )
        cnt = textDrawableList.addElementNotBoldAndBold(
            R.string.label_payeeTaxCode to model.labelPayeeTaxCode.orEmpty(),
            cnt, smallText, fontRegular to RUiKit.font.roboto_bold, DrawableGravity.Start to DrawableGravity.Start, true
        )
        cnt += 2
        if (status != Status.PRE_CLOSE) {
            cnt = textDrawableList.addElementsWithRoad(
                Triple(R.string.payment_managed_by, "Nexi Payments S.p.A.", "Corso Sempione, 55 · 20149, Milano"),
                cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
                Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
            )
        }
        cnt = textDrawableList.addElementsWithRoad(
            Triple(RShared.string.receipt_done_by, RShared.string.pago_pa_spa, RShared.string.pago_pa_road),
            cnt, Triple(smallText, smallText, smallText), Triple(fontRegular, fontBold, fontRegular),
            Triple(DrawableGravity.Center, DrawableGravity.Center, DrawableGravity.Center)
        )
        val terminalCode = "${getStringSafely(R.string.terminal)}: ${model.labelTerminalCode.orEmpty()}"
        textDrawableList.addOneTextDrawable(terminalCode, cnt, smallText, RUiKit.font.roboto_light, DrawableGravity.Center)
        receiptHeight += 150
        val imageList = ArrayList<ImageDrawable>()
        imageList.addOneImageDrawable(mainActivity, RUiKit.drawable.logo_nero, 0, 160, 90)
        val subject = getStringSafely(R.string.mail_subject)
        val body = "${getStringSafely(R.string.mail_body_title)}\n\n" +
                "${getStringSafely(R.string.transaction_id)}:${model.transactionID.orEmpty()}\n" +
                "${getStringSafely(R.string.label_noticeCode)}: ${model.labelNoticeCode.orEmpty()}\n" +
                "${getStringSafely(R.string.label_payeeTaxCode)}: ${model.labelPayeeTaxCode.orEmpty()}"
        QrCodeUtils().mailQrCode(mail, subject, body)?.let { qrCodeBitmap ->
            imageList.add(ImageDrawable(qrCodeBitmap.toDrawable(mainActivity.resources), 15, DrawableGravity.Center, 180, 180))
        }
        receiptHeight += 550
        return SecondScreenDrawable.withTextDrawables(textDrawableList).withImageDrawables(imageList).withBackGroundColor(R.color.white)
            .constructAsLinearLayout(mainActivity, cnt + 7)
    }

    private fun String.cutIfNeeded(): Pair<String, Int> {
        return if (this.length <= 20) this to 0
        else {
            val half = (this.length.toFloat() / 2f).roundToInt()
            this.substring(0, half) + "\n" + this.substring(half, this.length) to 1
        }
    }
}