package it.pagopa.swc_smartpos.view

import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.app_shared.network.BaseWrapper
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.ItemStoricoBinding
import it.pagopa.swc_smartpos.databinding.TansactionHistoryFragmentBinding
import it.pagopa.swc_smartpos.model.Status
import it.pagopa.swc_smartpos.model.Transaction
import it.pagopa.swc_smartpos.second_screen.showSecondScreenIntro
import it.pagopa.swc_smartpos.sharedutils.extensions.dateStringToTimestamp
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.disableScroll
import it.pagopa.swc_smartpos.view.view_shared.accessTokenLambda
import it.pagopa.swc_smartpos.view_model.StoricoViewModel
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit


class TransactionHistoryFragment : BaseDataBindingFragmentApp<TansactionHistoryFragmentBinding>() {
    override val backPress: () -> Unit get() = { findNavController().popBackStack(R.id.introFragment, false) }
    override fun viewBinding() = binding(TansactionHistoryFragmentBinding::inflate)
    private val viewModel: StoricoViewModel by viewModels()

    override fun setupOnCreate() {
        super.setupOnCreate()
        callTransactions()
    }

    override fun setupUI() {
        mainActivity?.showSecondScreenIntro()
        binding.rwStorico.disableScroll()
    }

    override fun setupObservers() {
        viewModel.transactions.observe(viewLifecycleOwner) {
            if (it != null)
                frontEndManagement(it.isEmpty(), it)
        }
    }

    override fun setupListeners() {
        binding.goToAssistance.setOnClickListener {
            findNavController().navigate(R.id.action_global_WebViewFragment)
        }
    }

    private fun frontEndManagement(isNoOp: Boolean, list: List<Transaction>?) {
        with(binding) {
            llNoOp.isVisible = isNoOp
            nsvHistory.isVisible = !isNoOp
            if (list != null)
                binding.rwStorico.adapter = AdapterStorico(list)
        }
    }

    private fun callTransactions() {
        mainActivity?.viewModel?.setLoaderText(getStringSafely(R.string.feedback_loading_generic))
        mainActivity?.viewModel?.showLoader(true to false)
        accessTokenLambda { bearer ->
            viewModel.callTransactionsHistory(mainActivity!!, bearer, mainActivity?.sdkUtils?.getCurrentBusiness()?.value)
                .observe(viewLifecycleOwner, BaseWrapper(mainActivity!!, {
                    it?.transactions?.let { transactions ->
                        viewModel.setTransactions(transactions)
                    } ?: run {
                        error()
                    }
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
            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer(getTextSafely(R.string.cta_goToHomepage)) {
                it.findNavController().popBackStack(R.id.introFragment, false)
            })
        })
    }

    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(RUiKit.drawable.menu_primary) { mainActivity?.showMenuBottomSheet() },
            null,
            HeaderView.HeaderElement(RUiKit.drawable.home_primary) { backToIntroFragment() },
            R.color.white
        )


    private inner class AdapterStorico(list: List<Transaction>) : BaseRecyclerView<Transaction, ItemStoricoBinding>(list) {
        override fun viewBinding() = binding(ItemStoricoBinding::inflate)
        override fun bind(context: Context, item: Transaction, pos: Int, binding: ItemStoricoBinding) {
            binding.root.setOnClickListener {
                val bundle = Bundle()
                bundle.putSerializable("transaction", item)
                navigate(R.id.action_transactionHistoryFragment_to_transactionDetailFragment, bundle)
            }
            binding.adviseCode.text = item.notices?.get(0)?.noticeNumber?.chunked(4)?.joinToString(" ").orEmpty()
            binding.timestamp.text = item.insertTimestamp?.dateStringToTimestamp()?.let {
                Date(it)
            }?.let {
                SimpleDateFormat(
                    "dd MMM yyyy',' HH:mm",
                    Locale.getDefault()
                ).format(it).uppercase()
            }
            val otherSymbols = DecimalFormatSymbols(Locale.getDefault())
            otherSymbols.decimalSeparator = ','
            otherSymbols.groupingSeparator = '.'
            val amount = item.totalAmount?.toAmountFormatted()
            binding.prezzo.text = amount
            bindStatus(item.getStato(), binding)
        }

        private fun bindStatus(status: Status, binding: ItemStoricoBinding) {
            binding.icon.setImageResource(
                when (status) {
                    Status.CLOSED -> RUiKit.drawable.success_image
                    Status.ERROR_ON_CLOSE, Status.ERROR_ON_RESULT -> RUiKit.drawable.warning_image
                    Status.PENDING, Status.PRE_CLOSE -> RUiKit.drawable.sospeso
                    Status.ABORT, Status.ERROR_ON_PAYMENT -> RUiKit.drawable.alert_image
                    Status.RIMBORSATA -> RUiKit.drawable.rimborsata
                }
            )
        }
    }
}