package it.pagopa.swc.smartpos.idpay.second_screen

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableGravity
import it.pagopa.swc.smartpos.app_shared.second_screen.ImageDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.SecondScreenDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.TextDrawable
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

fun MainActivity.showSecondScreenLoader(text: String, showIt: Boolean) {
    if (this.viewModel.hasSecondScreen.value) {
        if (pb == null) {
            pb = it.pagopa.swc.smartpos.app_shared.second_screen.DrawableProgressBar.construct(10f, 11f, RUiKit.color.primary, R.color.white, R.color.white)
        }
        if (showIt) {
            pb?.displayWithText(
                this, TextDrawable(
                    text, RUiKit.color.black, 0,
                    44f, RUiKit.font.readex_pro_bold,
                    DrawableGravity.Center
                )
            )
            pb?.launch()
        } else {
            pb?.cancelJob()
            viewModel.viewModelScope.launch {
                delay(200L)
                this@showSecondScreenLoader.sdkUtils?.displayDrawable(viewModel.currentSecondScreenDrawable.value)
            }
        }
    }
}

fun MainActivity?.showSecondScreenWelcome() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val original = ContextCompat.getDrawable(this, R.drawable.logo)
        val drawable = SecondScreenDrawable.withImageDrawables(arrayListOf(ImageDrawable(original, 0, DrawableGravity.Center)))
            .withBackGroundColor(RUiKit.color.primary).construct(this)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

private fun MainActivity?.showTextAndImageCentered(
    @StringRes textId: Int,
    @ColorRes textColor: Int,
    @DrawableRes image: Int,
    @ColorRes backGroundColor: Int,
    minWidth: Int = 80,
    minHeight: Int = 80
) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val mText = this.resources?.getString(textId)
        val drawable = this.imageDrawableAndTextCentered(mText, textColor, image, backGroundColor, minWidth, minHeight)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showSecondScreenIntro() {
    this.showTextAndImageCentered(R.string.id_pay_intro_second_screen, RUiKit.color.white, R.drawable.bonus, RUiKit.color.primary)
}

private fun MainActivity.imageDrawableAndTextCentered(
    text: String?,
    @ColorRes textColor: Int,
    @DrawableRes drawable: Int,
    @ColorRes backGround: Int,
    minWidth: Int = 0,
    minHeight: Int = 0
): Drawable {
    return SecondScreenDrawable.withTextDrawables(
        arrayListOf(
            TextDrawable(
                text.orEmpty(),
                textColor,
                7,
                60f,
                RUiKit.font.readex_pro, DrawableGravity.Center
            )
        )
    ).withImageDrawables(
        arrayListOf(
            ImageDrawable(
                ContextCompat.getDrawable(this, drawable),
                4, DrawableGravity.Center, minWidth, minHeight
            )
        )
    ).withBackGroundColor(backGround)
        .construct(this, 11)
}

fun MainActivity?.bindConfirmCieOperation() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val goodsImport = this.resources?.getString(R.string.amount)
        val initiative = this.resources?.getString(R.string.initiative)
        val availableSale = this.viewModel.model.value.availableSale?.toAmountFormatted()
        val toAuthorize = "${this.resources?.getString(R.string.to_authorize).orEmpty()}: $availableSale"
        val drawable = SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(
                    goodsImport.orEmpty().uppercase(), RUiKit.color.blue_grey_medium, 4, 44f,
                    it.pagopa.swc_smart_pos.ui_kit.R.font.readex_pro, DrawableGravity.Start
                ),
                TextDrawable(
                    this.viewModel.model.value.amount?.toAmountFormatted().orEmpty(), R.color.black, 6, 44f, RUiKit.font.titillium_web_semi_bold,
                    DrawableGravity.Start, underLine = TextDrawable.Line(RUiKit.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    initiative.orEmpty().uppercase(), RUiKit.color.blue_grey_medium, 10, 44f, RUiKit.font.readex_pro, DrawableGravity.Start
                ),
                TextDrawable(
                    this.viewModel.model.value.initiative?.name.orEmpty(), R.color.black, 12, 44f,
                    RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start,
                    underLine = TextDrawable.Line(RUiKit.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    toAuthorize, R.color.black, 17, 70f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start
                )
            )
        ).withBackGroundColor(R.color.white).construct(this, 20)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showInsertCieSecondScreen(situation: String) {
    fun MainActivity.insertCieBasic(
        situation: String,
        @ColorRes textColor: Int,
        @ColorRes backGround: Int,
        minWidth: Int = 0,
        minHeight: Int = 0
    ): Drawable {
        val basicText = this.resources?.getString(R.string.insert_cie).orEmpty()
        val (text, ballsDrawable, imageDrawable) = when (situation) {
            "TRANSMITTING" -> Triple(basicText, R.drawable.transmitting_second_screen, R.drawable.cie_small_image)
            "READ" -> Triple(
                this.resources?.getString(R.string.cie_read_description).orEmpty(),
                R.drawable.card_read_second_screen, R.drawable.second_screen_cie_success
            )

            "ERROR" -> Triple(
                this.resources?.getString(R.string.cie_failed_read_description).orEmpty(),
                R.drawable.error_card_second_screen, R.drawable.cie_small_image
            )

            else -> Triple(basicText, R.drawable.waiting_card_second_screen, R.drawable.cie_small_image)
        }
        return SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(
                    text,
                    textColor,
                    7,
                    60f,
                    RUiKit.font.readex_pro, DrawableGravity.Center
                )
            )
        ).withImageDrawables(
            arrayListOf(
                ImageDrawable(
                    ContextCompat.getDrawable(this, ballsDrawable),
                    1, DrawableGravity.Start, (minWidth.toFloat() * 2.5f).roundToInt(),
                    (minHeight.toFloat() * 0.4f).roundToInt()
                ),
                ImageDrawable(
                    ContextCompat.getDrawable(this, imageDrawable),
                    4, DrawableGravity.Center, minWidth, minHeight
                )
            )
        ).withBackGroundColor(backGround)
            .construct(this, 11)
    }
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val drawable = this.insertCieBasic(situation, RUiKit.color.black, RUiKit.color.grey_light, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showFocusQrSecondScreenWithCode(qrCodeImage: Bitmap?) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val textLeftFirst = this.resources?.getString(R.string.focus_qr_second_screen_first).orEmpty()
        val textLeftSecond = this.resources?.getString(R.string.focus_qr_second_screen_second).orEmpty()
        val textLeftThird = this.resources?.getString(R.string.focus_qr_second_screen_third).orEmpty()
        val drawable = SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(textLeftFirst, RUiKit.color.black, 7, 70f, RUiKit.font.readex_pro, DrawableGravity.Start),
                TextDrawable(textLeftSecond, RUiKit.color.black, 10, 70f, RUiKit.font.readex_pro, DrawableGravity.Start),
                TextDrawable(textLeftThird, RUiKit.color.black, 13, 70f, RUiKit.font.readex_pro, DrawableGravity.Start)
            )
        ).withImageDrawables(
            arrayListOf(
                ImageDrawable(
                    ContextCompat.getDrawable(this, RUiKit.drawable.rounded_white_filled_8dp),
                    10, minWidth = 337, minHeight = 330, customGravity = 0.63f
                ),
                ImageDrawable(qrCodeImage?.toDrawable(this.resources), 10, minWidth = 290, minHeight = 290, customGravity = 0.65f)
            )
        ).withBackGroundColor(RUiKit.color.grey_light).construct(this, 20)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showInsertTrxCodeSecondScreen(trxCode: String?) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val textFirst = this.resources?.getString(R.string.qr_trouble_question_description_first_second_screen).orEmpty()
        val textSecond = this.resources?.getString(R.string.qr_trouble_question_description_second_second_screen).orEmpty()
        val drawable = SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(textFirst, RUiKit.color.black, 5, 60f, RUiKit.font.readex_pro, DrawableGravity.Center),
                TextDrawable(textSecond, RUiKit.color.black, 7, 60f, RUiKit.font.readex_pro, DrawableGravity.Center),
                TextDrawable(trxCode.orEmpty(), RUiKit.color.black, 13, 100f, RUiKit.font.readex_pro, DrawableGravity.Center)
            )
        ).withImageDrawables(
            arrayListOf(
                ImageDrawable(
                    ContextCompat.getDrawable(this, RUiKit.drawable.rounded_white_filled_8dp),
                    12, minWidth = 750, minHeight = 180, customGravity = 0.2f
                )
            )
        ).withBackGroundColor(RUiKit.color.grey_light).construct(this, 20)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showCieCodeSecondScreen() {
    this.showTextAndImageCentered(R.string.insert_auth_code, RUiKit.color.black, R.drawable.lock_primary, RUiKit.color.grey_light)
}

fun MainActivity?.showWaitCitizenDecisionSecondScreen() {
    this.showTextAndImageCentered(R.string.continue_on_io_second_screen, RUiKit.color.info_dark, RUiKit.drawable.icon_info_loading, RUiKit.color.info_light)
}

fun MainActivity?.showBonusResult(text: String) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val drawable = this.imageDrawableAndTextCentered(
            text, RUiKit.color.success_dark, RUiKit.drawable.success_image,
            RUiKit.color.success_light, 80, 80
        )
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showDeletedOpResult() {
    this.showTextAndImageCentered(R.string.canceled_op_by_io_title, RUiKit.color.warning_dark, RUiKit.drawable.warning_image, RUiKit.color.warning_light)
}
fun MainActivity?.showPinAttemptsExhaustedResult() {
    this.showTextAndImageCentered(R.string.pin_attempts_exhausted_title, RUiKit.color.warning_dark, RUiKit.drawable.warning_image, RUiKit.color.warning_light)
}

fun MainActivity?.showMaxRetriesResult() {
    this.showTextAndImageCentered(R.string.session_expired_by_io_title, RUiKit.color.info_dark, RUiKit.drawable.info_image, RUiKit.color.info_light)
}

fun MainActivity?.showNeedReceiptToSecondScreen() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val text = this.resources?.getString(RShared.string.title_generateReceipt)
        val drawable = this.imageDrawableAndTextCentered(text, RUiKit.color.white, R.drawable.receipt, RUiKit.color.primary, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showSecondScreenOutro(hasResidual: Boolean) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = if (hasResidual) this.resources?.getString(RShared.string.title_flowCompleted) else this.resources?.getString(RShared.string.title_bye)
        val drawable = this.imageDrawableAndTextCentered(showQrText, RUiKit.color.white, R.drawable.bonus, RUiKit.color.primary, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}
