package it.pagopa.swc.smartpos.idpay.view

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.printer.PrintReceipt
import it.pagopa.swc.smartpos.idpay.second_screen.showNeedReceiptToSecondScreen
import it.pagopa.swc.smartpos.idpay.view.view_shared.print
import it.pagopa.swc.smartpos.idpay.view_model.ReceiptPaymentViewModel
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseReceiptFragment
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUIKit

class ReceiptFragment : BaseReceiptFragment<MainActivity>() {
    private val viewModel by viewModels<ReceiptPaymentViewModel>()
    override val backPress: () -> Unit get() = {}
    override val mainText: Int get() = RShared.string.title_generateReceipt
    override val secondaryText: Int get() = RShared.string.paragraph_receipt_suggestion
    override val mainImage: Int get() = R.drawable.receipt
    override val firstButton: CustomButton
        get() = CustomButton(RShared.string.cta_sendEmail, R.drawable.mail) {
        }
    private val isAndroidNative by lazy {
        BuildConfig.FLAVOR.contains("androidNative", true)
    }
    override val secondButton: CustomButton
        get() = CustomButton(
            if (isAndroidNative)
                RShared.string.share
            else
                RShared.string.cta_printReceipt,
            if (isAndroidNative)
                R.drawable.share
            else
                R.drawable.print
        ) {
            if (isAndroidNative)
                viewModel.isAndroidNativeShare = true
            mainActivity?.print(receiptDrawable(), true) {
                if (!isAndroidNative)
                    findNavController().navigate(R.id.action_receiptFragment_to_outroFragment)
            }
        }
    override val thirdButton: CustomButton
        get() = CustomButton(RShared.string.cta_noReceipt, R.drawable.no_receipt) {
            findNavController().navigate(R.id.action_receiptFragment_to_outroFragment)
        }

    override fun setupOnCreate() {
        super.setupOnCreate()
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.onUpdatingView = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                mainActivity?.viewModel?.foldableManagement?.updateView(this, binding.root, RUIKit.layout.receipt)
        }
        mainActivity?.showNeedReceiptToSecondScreen()
    }

    override fun onPause() {
        viewModel.isFromPause = viewModel.isAndroidNativeShare to true
        super.onPause()
        if (viewModel.isAndroidNativeShare)
            LoginUtility.shouldVerifySession = false
    }

    override fun setupOnResume() {
        if (viewModel.isFromPause.first && viewModel.isFromPause.second)
            findNavController().navigate(R.id.action_receiptFragment_to_outroFragment)
        viewModel.isAndroidNativeShare = false
        super.setupOnResume()
    }

    private fun receiptDrawable(): Pair<Drawable?, Int> {
        val instance = PrintReceipt(mainActivity)
        return if (mainActivity?.viewModel?.model?.value?.isCanceledOp == true)
            instance.receiptNotOkDrawable(mainActivity?.viewModel?.model?.value) to instance.receiptHeight
        else
            instance.receiptOkDrawable(mainActivity?.viewModel?.model?.value) to instance.receiptHeight
    }
}