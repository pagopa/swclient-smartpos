package it.pagopa.swc_smartpos.view.view_shared

import android.graphics.drawable.Drawable
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import it.pagopa.swc_smartpos.Application
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.printer.PrintReceipt
import it.pagopa.swc_smartpos.sharedutils.WrapperLogger
import it.pagopa.swc_smartpos.sharedutils.extensions.orEmptyCharSeq
import it.pagopa.swc_smartpos.sharedutils.interfaces.PrintStatus
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.dpToPxWith

fun Fragment.accessTokenLambda(action: (String) -> Unit) {
    val mainActivity = activity as? MainActivity
    mainActivity?.backPressEnabled(false)
    mainActivity?.decrypt(mainActivity.viewModel.accessToken.value, action)
}

fun MainActivity?.printWithMainViewModel(status: Status? = null, actionQueued: () -> Unit) {
    val print = PrintReceipt(this)
    val first = print.printReceipt(this?.viewModel?.receiptModel?.value, status)
    this.print(first to print.receiptHeight, true, actionQueued)
}

fun MainActivity?.print(
    receiptDrawable: Pair<Drawable?, Int>,
    showSecondScreenLoader: Boolean,
    actionQueued: (() -> Unit)? = null
) {
    if (this?.viewModel?.isPrinterAvailable?.value == true) {
        this.resources?.getString(R.string.feedback_loading_printReceipt)?.let {
            this.viewModel.setLoaderText(it)
            this.viewModel.showLoader(true to showSecondScreenLoader)
        }
        val (receipt, receiptHeight) = receiptDrawable
        receipt?.let {
            this.sdkUtils?.printDrawable(Application.instance.applicationContext, it, receiptHeight, object : PrintStatus {
                override fun queued() {
                    this@print.runOnUiThread {
                        this@print.viewModel.showLoader(false to false)
                        actionQueued?.invoke()
                    }
                }

                override fun failed() {
                    this@print.runOnUiThread {
                        this@print.viewModel.showLoader(false to false)
                        UiKitStyledDialog.withStyle(Style.Warning)
                            .withTitle(this@print.resources?.getText(R.string.title_printingError).orEmptyCharSeq())
                            .withDescription(this@print.resources?.getText(R.string.paragraph_printingError).orEmptyCharSeq())
                            .withMainBtn(this@print.resources?.getText(R.string.cta_retry).orEmptyCharSeq()) {
                                print(receiptDrawable, showSecondScreenLoader, actionQueued)
                            }.withSecondaryBtn(this@print.resources?.getText(R.string.cta_close).orEmptyCharSeq())
                            .showDialog(this@print.supportFragmentManager)
                    }
                    WrapperLogger.e("Printer", "FAILED")
                }
            })
        } ?: run {
            WrapperLogger.e("Printer", "FAILED due to null bitmap")
        }
    } else {
        UiKitStyledDialog.withStyle(Style.Warning)
            .withTitle(this@print?.resources?.getText(R.string.title_printingError).orEmptyCharSeq())
            .withDescription(this@print?.resources?.getText(R.string.paragraph_printingError).orEmptyCharSeq())
            .withMainBtn(this@print?.resources?.getText(R.string.cta_retry).orEmptyCharSeq()) {
                print(receiptDrawable, showSecondScreenLoader, actionQueued)
            }.withSecondaryBtn(this@print?.resources?.getText(R.string.cta_close).orEmptyCharSeq())
            .showDialog(this@print?.supportFragmentManager)
    }
}

fun LinearLayoutCompat.elevateFrameLayoutIfNeeded(nsv: NestedScrollView, fl: FrameLayout) {
    this.post {
        if (this.height > nsv.height) {
            context?.let { ctx -> fl.elevation = 16f dpToPxWith ctx }
        } else
            fl.elevation = 0f
    }
}