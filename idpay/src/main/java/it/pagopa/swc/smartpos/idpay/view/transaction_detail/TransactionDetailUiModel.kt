package it.pagopa.swc.smartpos.idpay.view.transaction_detail

import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.model.response.Transaction
import it.pagopa.swc.smartpos.idpay.model.response.TransactionStatus
import it.pagopa.swc_smartpos.sharedutils.extensions.dateStringToTimestamp
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

data class TransactionDetailUiModel(val transaction: Transaction?) : Serializable {
    fun transactionId() = transaction?.idpayTransactionId?.chunked(4)?.joinToString(" ")
    fun initiative() = transaction?.initiativeId.orEmpty()
    fun goodsCost() = transaction?.goodsCost?.toAmountFormatted().orEmpty()
    fun idPayBonus() = transaction?.coveredAmount?.toAmountFormatted().orEmpty()
    fun dateAndTime() = transaction?.lastUpdate?.dateStringToTimestamp()?.let {
        Date(it)
    }?.let {
        SimpleDateFormat(
            "dd MMM yyyy',' HH:mm",
            Locale.getDefault()
        ).format(it).uppercase()
    }

    private val threeDays by lazy {
        60 * 60 * 24 * 1000 * 3
    }

    fun cancelOpVisible(): Boolean {
        val dateString = transaction?.lastUpdate?.dateStringToTimestamp() ?: return false
        return Date().time - dateString < threeDays.toLong() && (transaction.status == TransactionStatus.AUTHORIZED.name || transaction.status == TransactionStatus.REWARDED.name)
    }

    fun executedOrNotText(fragment: TransactionDetailFragment) = when (transaction?.status) {
        TransactionStatus.REWARDED.name, TransactionStatus.AUTHORIZED.name -> fragment.getStringSafely(R.string.executed)
        else -> fragment.getStringSafely(R.string.canceled)
    }

    fun executedOrNotBackGround(fragment: TransactionDetailFragment) = when (transaction?.status) {
        TransactionStatus.REWARDED.name, TransactionStatus.AUTHORIZED.name -> AppCompatResources.getDrawable(
            fragment.requireContext(),
            RUiKit.drawable.badge_eseguita
        )

        else -> AppCompatResources.getDrawable(fragment.requireContext(), RUiKit.drawable.badge_in_sopseso)
    }

    fun executedOrNotTextColor(fragment: TransactionDetailFragment):Int = when (transaction?.status) {
        TransactionStatus.REWARDED.name, TransactionStatus.AUTHORIZED.name -> ContextCompat.getColor(
            fragment.requireContext(),
            RUiKit.color.success_dark
        )

        else -> ContextCompat.getColor(fragment.requireContext(), RUiKit.color.info_dark)
    }
}
