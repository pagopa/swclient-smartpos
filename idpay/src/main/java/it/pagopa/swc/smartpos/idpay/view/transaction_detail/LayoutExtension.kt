package it.pagopa.swc.smartpos.idpay.view.transaction_detail

import androidx.core.view.isVisible
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.ItemTransactionDetailBinding
import it.pagopa.swc.smartpos.app_shared.R as RShared

fun ItemTransactionDetailBinding.initDateAndTime(fragment: TransactionDetailFragment) {
    this.executedOrNot.isVisible = true
    this.title.text = fragment.getStringSafely(R.string.label_transactionTime).uppercase()
}

fun ItemTransactionDetailBinding.initGoodsCost(fragment: TransactionDetailFragment) {
    this.executedOrNot.isVisible = false
    this.title.text = fragment.getTextSafely(R.string.amount)
}

fun ItemTransactionDetailBinding.initTransactionId(fragment: TransactionDetailFragment) {
    this.executedOrNot.isVisible = false
    this.title.text = fragment.getStringSafely(RShared.string.transaction_id).uppercase()
}

fun ItemTransactionDetailBinding.initInitiative(fragment: TransactionDetailFragment) {
    this.executedOrNot.isVisible = false
    this.title.text = fragment.getStringSafely(R.string.initiative_id).uppercase()
}