package it.pagopa.swc_smartpos.view

import android.graphics.drawable.Drawable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.printer.PrintReceipt
import it.pagopa.swc_smartpos.second_screen.showNeedReceiptToSecondScreen
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseReceiptFragment
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.view.view_shared.print
import it.pagopa.swc_smartpos.view_model.ReceiptViewModel
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel

class ReceiptFragment : BaseReceiptFragment<MainActivity>() {
    private val viewModel: ReceiptViewModel by viewModels()
    private var isFromUiKit = false
    override val backPress: () -> Unit get() = { if (isFromUiKit) findNavController().navigateUp() }
    override val mainImage: Int get() = R.drawable.receipt
    override val mainText: Int get() = R.string.title_generateReceipt
    override val secondaryText: Int get() = R.string.paragraph_receipt_suggestion
    override val firstButton: CustomButton
        get() = CustomButton(R.string.cta_sendEmail, R.drawable.mail) {
        }
    override val secondButton: CustomButton
        get() = CustomButton(R.string.cta_printReceipt, R.drawable.print) {
            mainActivity?.print(receiptDrawable(), true) {
                if (!isFromUiKit)
                    findNavController().navigate(R.id.action_receiptFragment_to_outroFragment)
            }
        }
    override val thirdButton: CustomButton
        get() = CustomButton(R.string.cta_noReceipt, R.drawable.no_receipt) {
            if (!isFromUiKit)
                findNavController().navigate(R.id.action_receiptFragment_to_outroFragment)
        }

    private fun receiptDrawable(): Pair<Drawable?, Int> {
        val instance = PrintReceipt(mainActivity)
        return instance.printReceipt(viewModel.receiptModel.value) to instance.receiptHeight
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        arguments?.let {
            isFromUiKit = it.getBoolean(UiKitShowCase.uiKitRecognition)
        }
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.showNeedReceiptToSecondScreen()
        viewModel.setReceiptModel(
            if (isFromUiKit)
                ReceiptModel(
                    BaseResultFragment.State.Success,
                    "15 mar 2023, 16:44", "Comune di Controguerra", "77777777777",
                    "0000 0000 0000 0000 00", "TARI 2023", "150,00 €", "1,00 €", "151,00 €",
                    "517a-4216-840E-461f-B011-036A-0fd1-34E1", "CODICE.TERMINALE"
                )
            else
                mainActivity?.viewModel?.receiptModel?.value
        )
    }
}