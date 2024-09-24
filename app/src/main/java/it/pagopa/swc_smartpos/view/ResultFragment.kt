package it.pagopa.swc_smartpos.view

import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.second_screen.showResultToSecondScreenRespect
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import it.pagopa.swc_smartpos.view_model.receipt_model.ReceiptModel

class ResultFragment : BaseResultFragment<MainActivity>() {
    private var isFromUiKit = false
    private var isToBackHome = false
    private var isErrorAndCanceled = true
    override val backPress: () -> Unit get() = { if (isFromUiKit) findNavController().navigateUp() }
    override var state: State = State.Info
    override var firstButton: CustomBtnCustomizer? = null
    override var secondBtn: CustomBtnCustomizer? = null

    override fun setupOnCreate() {
        super.setupOnCreate()
        state = arguments?.getSerializableExtra(stateArg, State::class.java) ?: State.Info
        title = arguments?.getInt(titleArg) ?: 0
        description = arguments?.getInt(descriptionArg) ?: 0
        isFromUiKit = arguments?.getBoolean(UiKitShowCase.uiKitRecognition) ?: false
        isToBackHome = arguments?.getBoolean(backHome) ?: false
        firstButton = arguments?.getSerializableExtra(firstButtonArg, CustomBtnCustomizer::class.java)
        secondBtn = arguments?.getSerializableExtra(secondButtonArg, CustomBtnCustomizer::class.java)
        isErrorAndCanceled = arguments?.getBoolean(isErrorAndCanceledArg) ?: true
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    override fun setupUI() {
        super.setupUI()
        if (!isToBackHome)
            mainActivity.showResultToSecondScreenRespect(state, isErrorAndCanceled)
        if (!isFromUiKit && !isToBackHome)
            mainActivity?.viewModel?.setReceiptModel(ReceiptModel(state))
    }

    companion object {
        const val backHome = "backHome"
        const val isErrorAndCanceledArg = "isErrorAndCanceledArg"
    }
}