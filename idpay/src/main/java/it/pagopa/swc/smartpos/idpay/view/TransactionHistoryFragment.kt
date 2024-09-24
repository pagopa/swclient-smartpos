package it.pagopa.swc.smartpos.idpay.view

import android.content.Context
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.HistoryItemBinding
import it.pagopa.swc.smartpos.idpay.databinding.TransactionHistoryFragmentBinding
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenIntro
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view.transaction_detail.TransactionDetailFragment
import it.pagopa.swc.smartpos.idpay.view.view_shared.accessTokenLambda
import it.pagopa.swc.smartpos.idpay.view_model.TransactionHistoryViewModel
import it.pagopa.swc_smartpos.sharedutils.extensions.dateStringToTimestamp
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerViewStableIds
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.disableScroll
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import it.pagopa.swc.smartpos.app_shared.R as RShared
import it.pagopa.swc_smart_pos.ui_kit.R as RUikit

class TransactionHistoryFragment : BaseDataBindingFragmentApp<TransactionHistoryFragmentBinding>() {
    override val backPress: () -> Unit get() = { backToIntroFragment() }
    override val layoutId: Int get() = R.layout.transaction_history_fragment
    override fun viewBinding() = binding(TransactionHistoryFragmentBinding::inflate)
    private val viewModel: TransactionHistoryViewModel by viewModels()
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.menu_primary) { mainActivity?.showMenuBottomSheet() },
            null,
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary) { backToIntroFragment() },
            R.color.white
        )

    override fun setupOnCreate() {
        super.setupOnCreate()
        callTransactions()
    }

    override fun setupListeners() {
        binding.goToAssistance.setOnClickListener {
            findNavController().navigate(R.id.action_global_WebViewFragment)
        }
    }

    override fun setupObservers() {
        viewModel.viewModelScope.launch {
            viewModel.transactions.collectLatest {
                frontEndManagement(it == null || it.transactions.isNullOrEmpty(), it?.transactions)
            }
        }
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.showSecondScreenIntro()
        binding.historyRw.disableScroll()
    }

    private fun frontEndManagement(isNoOp: Boolean, transactions: List<Transaction>?) {
        with(binding) {
            llNoOp.isVisible = isNoOp
            nsvHistory.isVisible = !isNoOp
            if (transactions != null)
                binding.historyRw.adapter = HistoryAdapter(transactions)
        }
    }

    private fun callTransactions() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(RShared.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to false)
        accessTokenLambda { bearer ->
            viewModel.callTransactionsHistory(mainActivity!!, bearer, mainActivity?.sdkUtils?.getCurrentBusiness()?.value)
                .observe(viewLifecycleOwner, BaseWrapper(mainActivity!!, {
                    viewModel.setTransactions(it)
                }, {
                    if (it == BaseWrapper.tokenRefreshed)
                        callTransactions()
                    else
                        error()
                }, showSecondScreenLoader = false, showDialog = false))
        }
    }

    private fun error() {
        findNavController().navigate(R.id.action_global_resultFragment, Bundle().apply {
            putBoolean(ResultFragment.backHome, true)
            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Error)
            this.putInt(BaseResultFragment.titleArg, R.string.title_errorLoadingTransactionList)
            this.putInt(BaseResultFragment.descriptionArg, R.string.paragraph_tryAgain)
            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer(getTextSafely(RShared.string.cta_goToHomepage)) {
                it.findNavController().popBackStack(R.id.introFragment, false)
            })
        })
    }

    private inner class HistoryAdapter(list: List<Transaction>) :
        BaseRecyclerViewStableIds<Transaction, HistoryItemBinding>(list) {
        override fun viewBinding() = binding(HistoryItemBinding::inflate)
        override fun bind(context: Context, item: Transaction, pos: Int, binding: HistoryItemBinding) {
            val canClick = item.status == TransactionStatus.AUTHORIZED.name ||
                    item.status == TransactionStatus.REWARDED.name ||
                    item.status == TransactionStatus.REJECTED.name ||
                    item.status == TransactionStatus.CANCELLED.name
            binding.icon.setImageDrawable(
                when (item.status) {
                    TransactionStatus.AUTHORIZED.name, TransactionStatus.REWARDED.name -> AppCompatResources.getDrawable(context, RUikit.drawable.success_image)
                    TransactionStatus.REJECTED.name, TransactionStatus.CANCELLED.name -> AppCompatResources.getDrawable(
                        context,
                        RUikit.drawable.transaction_canceled
                    )

                    else -> AppCompatResources.getDrawable(context, RUikit.drawable.warning_image)
                }
            )
            binding.initiativeName.text = item.initiativeId.orEmpty()
            binding.idPayBonus.text = item.coveredAmount?.toAmountFormatted()
            binding.timestampCreated.text = item.lastUpdate?.dateStringToTimestamp()?.let {
                Date(it)
            }?.let {
                SimpleDateFormat(
                    "dd MMM yyyy',' HH:mm",
                    Locale.getDefault()
                ).format(it).uppercase()
            }
            binding.root.setOnClickListener {
                if (canClick) {
                    this@TransactionHistoryFragment.findNavController().navigate(
                        R.id.action_transactionHistoryFragment_to_transactionDetailFragment,
                        bundleOf(TransactionDetailFragment.transaction to item)
                    )
                }
            }
        }
    }
}