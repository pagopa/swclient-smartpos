package it.pagopa.swc.smartpos.idpay.view.transaction_detail

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.login.LoginUtility
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.BuildConfig
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.TransactionDetailFragmentBinding
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.OutroFragment
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.print
import it.pagopa.swc.smartpos.idpay.view_model.TransactionDetailViewModel
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

class TransactionDetailFragment : BaseDataBindingFragmentApp<TransactionDetailFragmentBinding>() {
    private val viewModel by viewModels<TransactionDetailViewModel>()
    override val layoutId: Int get() = R.layout.transaction_detail_fragment
    override val backPress: () -> Unit get() = { findNavController().navigateUp() }
    override fun viewBinding() = binding(TransactionDetailFragmentBinding::inflate)
    private val dialogCancelOp by lazy {
        UiKitDialog.withTitle(getTextSafely(R.string.dialog_cancel_op_title))
            .withDescription(getTextSafely(R.string.dialog_cancel_op_description))
            .withMainBtn(getTextSafely(R.string.dialog_cancel_op_delete))
            .withSecondaryBtn(getTextSafely(R.string.cancel_op)) {
                cancelOp { transaction ->
                    mainActivity?.viewModel?.setModel(
                        SaleModel(
                            Initiatives.InitiativeModel(
                                "0", transaction?.initiativeId.orEmpty(), ""
                            ),
                            transaction?.goodsCost,
                            transaction?.coveredAmount,
                            100f,
                            transaction?.milTransactionId,
                            transaction?.lastUpdate,
                            true
                        )
                    )
                    findNavController().navigate(R.id.action_global_resultFragment, bundleOf(
                        BaseResultFragment.stateArg to BaseResultFragment.State.Success,
                        BaseResultFragment.titleArg to R.string.operation_canceled,
                        BaseResultFragment.descriptionArg to R.string.operation_canceled_descr,
                        BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                            getTextSafely(R.string.cta_continue),
                            RUiKit.drawable.arrow_right,
                            false
                        ) {
                            it.findNavController()
                                .navigate(R.id.action_resultFragment_to_receiptFragment)
                        }
                    ))
                }
            }
    }
    private val dialog by lazy {
        UiKitDialog.withTitle(getTextSafely(RShared.string.title_generateReceipt))
            .withCloseVisible()
            .withMainCustomBtn(
                CustomBtnCustomizer(
                    getTextSafely(RShared.string.cta_sendEmail),
                    R.drawable.mail_white,
                    true,
                    this
                ) {

                })
            .withSecondaryCustomBtn(CustomBtnCustomizer(
                if (isAndroidNative)
                    getTextSafely(RShared.string.share)
                else
                    getTextSafely(RShared.string.cta_printReceipt),
                if (isAndroidNative)
                    R.drawable.share_primary
                else
                    R.drawable.print_primary, true, this
            ) {
                (it as TransactionDetailFragment).viewModel.print(mainActivity)?.let { pair ->
                    if (isAndroidNative)
                        viewModel.isAndroidNativeShare = true
                    mainActivity?.print(pair, false) {
                        if (!isAndroidNative)
                            it.navigateToOutro()
                    }
                }
            })
    }
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
            null,
            HeaderView.HeaderElement(RUiKit.drawable.home_primary) {
                this.backToIntroFragment()
            },
            R.color.white
        )

    private fun Fragment.navigateToOutro() {
        this.findNavController()
            .navigate(R.id.action_transactionDetailFragment_to_outroFragment, Bundle().apply {
                putBoolean(OutroFragment.fromDetail, true)
            })
    }

    override fun setupOnCreate() {
        super.setupOnCreate()
        val transaction = arguments?.getSerializableExtra(transaction, Transaction::class.java)
        viewModel.setUiModel(TransactionDetailUiModel(transaction))
    }

    override fun onPause() {
        viewModel.isFromPause = viewModel.isAndroidNativeShare to true
        super.onPause()
        if (viewModel.isAndroidNativeShare)
            LoginUtility.shouldVerifySession = false
    }

    override fun setupOnResume() {
        if (viewModel.isFromPause.first && viewModel.isFromPause.second)
            this.navigateToOutro()
        viewModel.isAndroidNativeShare = false
        super.setupOnResume()
    }

    private val isAndroidNative by lazy {
        BuildConfig.FLAVOR.contains("androidNative", true)
    }

    override fun setupListeners() {
        binding.doReceipt.setOnClickListener {
            dialog.showDialog(activity?.supportFragmentManager)
        }
        binding.cancelOp.setOnClickListener {
            dialogCancelOp.showDialog(activity?.supportFragmentManager)
        }
    }

    override fun setupUI() {
        super.setupUI()
        binding.layoutDateAndTime.initDateAndTime(this)
        binding.layoutGoodsCost.initGoodsCost(this)
        binding.layoutInitiative.initInitiative(this)
        binding.layoutTransactionId.initTransactionId(this)
    }

    override fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiModel.collectLatest { uiModel ->
                    uiModel?.let {
                        binding.layoutDateAndTime.description.text = it.dateAndTime().orEmpty()
                        binding.layoutDateAndTime.executedOrNot.text =
                            it.executedOrNotText(this@TransactionDetailFragment)
                        binding.layoutDateAndTime.executedOrNot.background =
                            it.executedOrNotBackGround(this@TransactionDetailFragment)
                        binding.layoutDateAndTime.executedOrNot.setTextColor(
                            it.executedOrNotTextColor(
                                this@TransactionDetailFragment
                            )
                        )
                        binding.layoutTransactionId.description.text = it.transactionId().orEmpty()
                        binding.layoutInitiative.description.text = it.initiative()
                        binding.layoutGoodsCost.description.text = it.goodsCost()
                        binding.idPayBonus.text = it.idPayBonus()
                        binding.cancelOp.isVisible = it.cancelOpVisible()
                    }
                }
            }
        }
    }

    private fun cancelOp(actionDone: (Transaction?) -> Unit) {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.cancelOp(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                viewModel.uiModel.value?.transaction?.milTransactionId.orEmpty()
            ).observe(
                viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
                    actionDone.invoke(it)
                }, errorAction = {
                    if (it == BaseWrapper.tokenRefreshed)
                        cancelOp(actionDone)
                }, showLoader = true, showDialog = true)
            )
        }
    }

    companion object {
        const val transaction = "Transaction"
    }
}