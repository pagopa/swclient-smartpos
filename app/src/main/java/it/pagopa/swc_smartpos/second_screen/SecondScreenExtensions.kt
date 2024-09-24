package it.pagopa.swc_smartpos.second_screen

import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableGravity
import it.pagopa.swc.smartpos.app_shared.second_screen.DrawableProgressBar
import it.pagopa.swc.smartpos.app_shared.second_screen.ImageDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.SecondScreenDrawable
import it.pagopa.swc.smartpos.app_shared.second_screen.TextDrawable
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import it.pagopa.swc_smartpos.network.Api
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.view.PaymentReceiptFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

fun MainActivity.showSecondScreenLoader(text: String, showIt: Boolean) {
    if (this.viewModel.hasSecondScreen.value) {
        if (pb == null) {
            pb = DrawableProgressBar.construct(10f, 11f, R.color.primary, R.color.white, R.color.white)
        }
        if (showIt) {
            pb?.displayWithText(
                this, TextDrawable(
                    text, R.color.black, 0,
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
        val original = ContextCompat.getDrawable(this, RUiKit.drawable.logo)
        val drawable = SecondScreenDrawable.withImageDrawables(arrayListOf(ImageDrawable(original, 0, DrawableGravity.Center)))
            .withBackGroundColor(R.color.primary).construct(this)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
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

fun MainActivity?.showSecondScreenIntro() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = this.resources?.getString(R.string.title_payNotice)
        val drawable = this.imageDrawableAndTextCentered(showQrText, R.color.white, RUiKit.drawable.logo, R.color.primary)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showSecondScreenInsertManually() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = this.resources?.getString(R.string.title_showCodes)
        val drawable = this.imageDrawableAndTextCentered(showQrText, R.color.white, R.drawable.payment_advise, R.color.primary, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showQrCodeSample() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = this.resources?.getString(R.string.title_showQrCode)
        val drawable = this.imageDrawableAndTextCentered(showQrText, R.color.white, R.drawable.qrcode_sample, R.color.primary, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun QrCodeVerifyResponse.bindSecondScreen(mainActivity: MainActivity?) {
    if (mainActivity?.viewModel?.hasSecondScreen?.value == true) {
        val companyNameTitle = mainActivity.resources?.getString(R.string.label_payee)
        val paymentObjTitle = mainActivity.resources?.getString(R.string.label_paymentReason)
        val amountTitle = mainActivity.resources?.getString(R.string.label_updatedAmount)
        val drawable = SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(
                    companyNameTitle.orEmpty().uppercase(), R.color.blue_grey_medium, 3, 44f,
                    RUiKit.font.readex_pro, DrawableGravity.Start
                ),
                TextDrawable(
                    this.company, R.color.black, 5, 44f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start,
                    underLine = TextDrawable.Line(R.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    paymentObjTitle.orEmpty().uppercase(),
                    R.color.blue_grey_medium,
                    9,
                    44f,
                    RUiKit.font.readex_pro,
                    DrawableGravity.Start
                ),
                TextDrawable(
                    this.description, R.color.black, 11, 44f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start,
                    underLine = TextDrawable.Line(R.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    amountTitle.orEmpty().uppercase(), R.color.blue_grey_medium, 15, 44f,
                    RUiKit.font.readex_pro, DrawableGravity.Start
                ),
                TextDrawable(
                    this.amountFormatted(), R.color.black, 17, 44f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start
                )
            )
        ).withBackGroundColor(R.color.white).construct(mainActivity, 20)
        mainActivity.viewModel.setSecondScreenDrawable(drawable)
        mainActivity.sdkUtils?.displayDrawable(drawable)
    }
}

fun PaymentReceiptFragment.Model.bindSecondScreen(mainActivity: MainActivity?) {
    if (mainActivity?.viewModel?.hasSecondScreen?.value == true) {
        val amountTitle = mainActivity.resources?.getString(R.string.label_updatedAmount)
        val fee = mainActivity.resources?.getString(R.string.label_fee)
        val total = mainActivity.resources?.getString(R.string.label_toBePaid)
        val drawable = SecondScreenDrawable.withTextDrawables(
            arrayListOf(
                TextDrawable(
                    amountTitle.orEmpty().uppercase(), R.color.blue_grey_medium, 3, 44f,
                    RUiKit.font.readex_pro, DrawableGravity.Start
                ),
                TextDrawable(
                    this.amount.toAmountFormatted(), R.color.black, 5, 44f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start,
                    underLine = TextDrawable.Line(R.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    fee.orEmpty().uppercase(),
                    R.color.blue_grey_medium,
                    9,
                    44f,
                    RUiKit.font.readex_pro,
                    DrawableGravity.Start
                ),
                TextDrawable(
                    this.fee.toAmountFormatted(), R.color.black, 11, 44f, RUiKit.font.titillium_web_semi_bold, DrawableGravity.Start,
                    underLine = TextDrawable.Line(R.color.grey_ultra_light, 5, 50)
                ),
                TextDrawable(
                    "${total.orEmpty()}: ${this.totalAmount.toAmountFormatted()}", R.color.black, 16, 70f,
                    RUiKit.font.readex_pro, DrawableGravity.Start
                )
            )
        ).withBackGroundColor(R.color.white).construct(mainActivity, 20)
        mainActivity.viewModel.setSecondScreenDrawable(drawable)
        mainActivity.sdkUtils?.displayDrawable(drawable)
    }
}
 fun MainActivity?.showResultToSecondScreenRespect(state: BaseResultFragment.State, isErrorAndCanceled: Boolean = true) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val textToShowId: Int
        val drawableImage: Int
        val textColor: Int
        val baseColor: Int
        when (state) {
            BaseResultFragment.State.Success -> {
                textToShowId = R.string.title_paymentCompleted
                drawableImage = RUiKit.drawable.success_image
                textColor = RUiKit.color.success_dark
                baseColor = RUiKit.color.success_light
            }

            BaseResultFragment.State.Error -> {
                textToShowId = if (isErrorAndCanceled) R.string.title_transactionCancelled else R.string.title_authorizationDenied
                drawableImage = RUiKit.drawable.alert_image
                textColor = RUiKit.color.error_dark
                baseColor = RUiKit.color.error_light
            }

            BaseResultFragment.State.Info -> {
                textToShowId = R.string.title_secondScreen_uncertainOutcome
                drawableImage = RUiKit.drawable.info_image
                textColor = RUiKit.color.info_dark
                baseColor = RUiKit.color.info_light
            }

            BaseResultFragment.State.Warning -> {
                textToShowId = R.string.title_secondScreen_koOutcome
                drawableImage = RUiKit.drawable.warning_image
                textColor = RUiKit.color.warning_dark
                baseColor = RUiKit.color.warning_light
            }
        }
        val textToShow = this.resources?.getString(textToShowId)
        val drawable = this.imageDrawableAndTextCentered(textToShow, textColor, drawableImage, baseColor, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

infix fun MainActivity?.showResultToSecondScreenRespectApiErrorCode(string: String) {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val textToShowId: Int
        val drawableImage: Int
        val textColor: Int
        val baseColor: Int
        when (string) {
            Api.ErrorCode.WRONG_NOTICE_DATA, Api.ErrorCode.PAYMENT_ALREADY_IN_PROGRESS,
            Api.ErrorCode.REVOKED_NOTICE, Api.ErrorCode.EXPIRED_NOTICE, Api.ErrorCode.UNKNOWN_NOTICE -> {
                textToShowId = R.string.title_noticeWarning
                drawableImage = RUiKit.drawable.warning_image
                textColor = R.color.warning_dark
                baseColor = RUiKit.color.warning_light
            }

            Api.ErrorCode.NOTICE_ALREADY_PAID -> {
                textToShowId = R.string.paragraph_noticeAlreadyPaid
                drawableImage = RUiKit.drawable.info_image
                textColor = R.color.info_dark
                baseColor = RUiKit.color.info_light
            }

            "400" -> {
                textToShowId = R.string.title_invalidQrCode
                drawableImage = RUiKit.drawable.warning_image
                textColor = R.color.warning_dark
                baseColor = RUiKit.color.warning_light
            }

            else -> {
                textToShowId = R.string.title_noticeError
                drawableImage = RUiKit.drawable.alert_image
                textColor = R.color.error_dark
                baseColor = RUiKit.color.error_light
            }
        }
        val textToShow = this.resources?.getString(textToShowId)
        val drawable = this.imageDrawableAndTextCentered(textToShow, textColor, drawableImage, baseColor, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showNeedReceiptToSecondScreen() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = this.resources?.getString(R.string.title_generateReceipt)
        val drawable = this.imageDrawableAndTextCentered(showQrText, R.color.white, R.drawable.receipt, R.color.primary, 80, 80)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}

fun MainActivity?.showSecondScreenOutro() {
    if (this?.viewModel?.hasSecondScreen?.value == true) {
        val showQrText = this.resources?.getString(R.string.title_bye)
        val drawable = this.imageDrawableAndTextCentered(showQrText, R.color.white, RUiKit.drawable.logo, R.color.primary)
        this.viewModel.setSecondScreenDrawable(drawable)
        this.sdkUtils?.displayDrawable(drawable)
    }
}