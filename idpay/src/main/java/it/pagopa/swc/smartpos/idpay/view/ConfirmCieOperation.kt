package it.pagopa.swc.smartpos.idpay.view

import android.annotation.SuppressLint
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.ConfirmCieOpBinding
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.model.response.VerifyCieResponse
import it.pagopa.swc.smartpos.idpay.second_screen.bindConfirmCieOperation
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.view_shared.acceptNewBonus
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view.view_shared.genericErrorDialog
import it.pagopa.swc.smartpos.idpay.view_model.ConfirmCIeOperationViewModel
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import java.io.Serializable
import it.pagopa.swc.smartpos.app_shared.R as RShared

class ConfirmCieOperation : BaseDataBindingFragmentApp<ConfirmCieOpBinding>() {
    private val viewModel: ConfirmCIeOperationViewModel by viewModels()
    override fun viewBinding() = binding(ConfirmCieOpBinding::inflate)
    override val layoutId: Int get() = R.layout.confirm_cie_op
    override val backPress: () -> Unit = { }
    private val exitDialog by lazy {
        UiKitDialog
            .withTitle(getTextSafely(R.string.exit_from_current_op))
            .withDescription(getTextSafely(R.string.exit_from_current_op_description))
            .withMainBtn(getTextSafely(R.string.cta_cancel))
            .withSecondaryBtn(getTextSafely(R.string.exit_payment)) {
                cancelOp {
                    this.backToIntroFragment()
                }
            }
    }

    override val header: HeaderView = HeaderView(
        null,
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) {
            exitDialog.showDialog(mainActivity?.supportFragmentManager)
        },
        R.color.white
    )

    override fun setupOnCreate() {
        super.setupOnCreate()
        viewModel.setUiModel(
            arguments?.getSerializableExtra(
                uiModelConfirmCIeOp,
                UiModel::class.java
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun setupUI() {
        super.setupUI()
        mainActivity?.bindConfirmCieOperation()
        binding.initiativeName.text =
            mainActivity?.viewModel?.model?.value?.initiative?.name.orEmpty()
        binding.importValue.text = viewModel.uiModel.value?.euroCents?.toAmountFormatted()
        val totalValueFromBonus = viewModel.uiModel.value?.coveredAmount?.toAmountFormatted()
        binding.totalValue.text = totalValueFromBonus
    }

    override fun setupListeners() {
        binding.btnAuth.setOnClickListener {
            viewModel.uiModel.value?.let { model ->
                navigate(
                    R.id.action_confirmCieOperation_to_insertCiePinFragment, bundleOf(
                        InsertCiePinFragment.uiModel to InsertCiePinFragment.UiModel(
                            euroCents = model.euroCents,
                            epochMillis = model.epochMillis,
                            nis = model.nis,
                            secondFactor = model.secondFactor,
                            response = model.response,
                            transactionId = model.transactionId
                        )
                    )
                )
            }
        }
        binding.btnDeny.setOnClickListener {
            cancelOp(false) { bearer ->
                navigate(
                    R.id.action_global_resultFragment, bundleOf(
                        BaseResultFragment.stateArg to BaseResultFragment.State.Warning,
                        BaseResultFragment.titleArg to R.string.canceled_op_by_io_title,
                        BaseResultFragment.firstButtonArg to CustomBtnCustomizer(
                            getTextSafely(R.string.accept_new_bonus)
                        ) {
                            val frag = it as ResultFragment
                            frag.acceptNewBonus(frag.viewModel)
                        }, BaseResultFragment.secondButtonArg to CustomBtnCustomizer(
                            getTextSafely(RShared.string.cta_retry)
                        ) {
                            val frag = it as ResultFragment
                            frag.viewModel.createNewTransaction(
                                frag,
                                bearer = bearer,
                                errorAction = {
                                    //TODO Verify fallback with P&D
                                    frag.mainActivity?.genericErrorDialog()
                                }) { newTransactionId ->
                                frag.mainActivity?.viewModel?.setKeyBackStack("newTransactionId" to newTransactionId)
                                frag.findNavController().popBackStack(R.id.cieReaderFragment, false)
                            }
                        },
                        ResultFragment.operationState to ResultFragment.OperationState.DELETED
                    )
                )
            }
        }
    }

    private fun cancelOp(toVoidAllModel: Boolean = true, actionDone: (bearer: String) -> Unit) {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to true)
        accessTokenLambda { bearer ->
            viewModel.cancelOp(
                mainActivity!!,
                bearer,
                mainActivity?.sdkUtils?.getCurrentBusiness()?.value,
                viewModel.uiModel.value?.transactionId.orEmpty()
            ).observe(
                viewLifecycleOwner, BaseWrapper(mainActivity, successAction = {
                    mainActivity?.viewModel?.setModel(SaleModel(timeStamp = it?.lastUpdate))
                    if (toVoidAllModel)
                        mainActivity?.viewModel?.voidModel()
                    actionDone.invoke(bearer)
                }, errorAction = {
                    if (it == BaseWrapper.tokenRefreshed)
                        cancelOp(toVoidAllModel, actionDone)
                }, showLoader = true, showDialog = true)
            )
        }
    }

    data class UiModel(
        val euroCents: Long,
        val epochMillis: Long,
        val nis: String,
        val response: VerifyCieResponse,
        val transactionId: String,
        val coveredAmount: Long,
        val secondFactor: String
    ) : Serializable

    companion object {
        const val uiModelConfirmCIeOp = "uiModelConfirmCIeOp"
    }
}