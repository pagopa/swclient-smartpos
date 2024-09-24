package it.pagopa.swc.smartpos.idpay.view

import android.os.Build
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.second_screen.showBonusResult
import it.pagopa.swc.smartpos.idpay.second_screen.showDeletedOpResult
import it.pagopa.swc.smartpos.idpay.second_screen.showMaxRetriesResult
import it.pagopa.swc.smartpos.idpay.second_screen.showPinAttemptsExhaustedResult
import it.pagopa.swc.smartpos.idpay.view_model.ResultFragmentViewModel
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable
import it.pagopa.swc_smart_pos.ui_kit.R as RUIKit

class ResultFragment : BaseResultFragment<MainActivity>() {
    val viewModel: ResultFragmentViewModel by viewModels()
    private var isToBackHome = false
    private var isErrorAndCanceled = true
    override val backPress: () -> Unit get() = { }
    override var state: State = State.Info
    override var firstButton: CustomBtnCustomizer? = null
    override var secondBtn: CustomBtnCustomizer? = null

    override fun setupOnCreate() {
        super.setupOnCreate()
        state = arguments?.getSerializableExtra(stateArg, State::class.java) ?: State.Info
        title = arguments?.getInt(titleArg) ?: 0
        titleArgument = arguments?.getString(titleArgumentConstant, "").orEmpty()
        description = arguments?.getInt(descriptionArg) ?: 0
        isToBackHome = arguments?.getBoolean(backHome) ?: false
        firstButton = arguments?.getSerializableExtra(firstButtonArg, CustomBtnCustomizer::class.java)
        secondBtn = arguments?.getSerializableExtra(secondButtonArg, CustomBtnCustomizer::class.java)
        isErrorAndCanceled = arguments?.getBoolean(isErrorAndCanceledArg) ?: true
        descriptionArgument = arguments?.getString(descriptionArgumentConstant, "").orEmpty()
        mainActivity?.viewModel?.setKeepScreenOn(false)
        viewModel.setOpState(arguments?.getSerializableExtra(operationState, OperationState::class.java))
    }

    override fun setupUI() {
        super.setupUI()

        mainActivity?.onUpdatingView = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                mainActivity?.viewModel?.foldableManagement?.updateView(this, binding.root, RUIKit.layout.result)
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.operationState.collectLatest {
                    when (it) {
                        OperationState.DELETED -> mainActivity?.showDeletedOpResult()
                        OperationState.MAX_RETRIES -> mainActivity?.showMaxRetriesResult()
                        OperationState.PIN_ATTEMPTS_EXHAUSTED -> mainActivity?.showPinAttemptsExhaustedResult()
                        else -> mainActivity?.showBonusResult(binding.tvTitle.text.toString())
                    }
                }
            }
        }
        /*if (!isToBackHome)
            mainActivity.showResultToSecondScreenRespect(state, isErrorAndCanceled)
        if (!isToBackHome)
             mainActivity?.viewModel?.setReceiptModel(ReceiptModel(state))*/
    }

    enum class OperationState : Serializable {
        DELETED,
        MAX_RETRIES,
        PIN_ATTEMPTS_EXHAUSTED,
        OK
    }

    companion object {
        const val backHome = "backHome"
        const val isErrorAndCanceledArg = "isErrorAndCanceledArg"
        const val operationState = "operationState"
    }
}