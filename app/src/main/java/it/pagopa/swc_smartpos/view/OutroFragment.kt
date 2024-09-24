package it.pagopa.swc_smartpos.view

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.MainActivity
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.second_screen.showSecondScreenOutro
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseOutroFragment
import it.pagopa.swc_smartpos.view_model.OutroViewModel

class OutroFragment : BaseOutroFragment<MainActivity>() {
    private val viewModel: OutroViewModel by viewModels()
    private var isFromUiKit = false
    private var isFromDetail = false
    override val backPress: () -> Unit get() = { if (isFromUiKit) findNavController().navigateUp() }
    override val homeAction: () -> Unit get() = { findNavController().popBackStack(R.id.introFragment, false) }
    override val mainText: Int get() = R.string.title_flowCompleted
    override var descriptionText: Int = R.string.paragraph_receipt_suggestion
    override var btnText: Int = R.string.cta_newPayment
    override val btnAction: () -> Unit get() = { findNavController().popBackStack(R.id.introFragment, false) }

    override fun setupOnCreate() {
        super.setupOnCreate()
        arguments?.let {
            isFromUiKit = it.getBoolean(UiKitShowCase.uiKitRecognition)
            isFromDetail = it.getBoolean(fromDetail)
        }
        mainActivity?.viewModel?.setKeepScreenOn(false)
    }

    override fun setupUI() {
        if (isFromDetail) {
            descriptionText = R.string.paragraph_receipt_printed
            btnText = R.string.cta_goToHomepage
        }
        super.setupUI()
        if (!isFromDetail)
            mainActivity?.showSecondScreenOutro()
        viewModel.oneMinuteMaxInFragment(homeAction)
    }

    companion object {
        const val fromDetail = "fromDetailArg"
    }
}