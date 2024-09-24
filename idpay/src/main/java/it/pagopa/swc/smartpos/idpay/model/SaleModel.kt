package it.pagopa.swc.smartpos.idpay.model

import kotlinx.coroutines.flow.StateFlow
import java.io.Serializable

data class SaleModel(
    var initiative: Initiatives.InitiativeModel? = null,
    var amount: Long? = null,
    var availableSale: Long? = null,
    var percentageSale: Float? = null,
    var milTransactionId: String? = null,
    var timeStamp: String? = null,
    var isCanceledOp: Boolean? = false
) : Serializable {
    private fun <T> setParamCorrectly(current: T?, param: T?): T? {
        return when {
            current == null -> param
            current != param && param != null -> param
            else -> current
        }
    }

    fun set(currentFlow: StateFlow<SaleModel>, value: SaleModel) {
        initiative = setParamCorrectly(currentFlow.value.initiative, value.initiative)
        amount = setParamCorrectly(currentFlow.value.amount, value.amount)
        percentageSale = setParamCorrectly(currentFlow.value.percentageSale, value.percentageSale)
        availableSale = setParamCorrectly(currentFlow.value.availableSale, value.availableSale)
        milTransactionId = setParamCorrectly(currentFlow.value.milTransactionId, value.milTransactionId)
        timeStamp = setParamCorrectly(currentFlow.value.timeStamp, value.timeStamp)
        isCanceledOp = setParamCorrectly(currentFlow.value.isCanceledOp, value.isCanceledOp)
    }

    fun humanPercentage(): String? {
        if (percentageSale == null) return null
        val value = percentageSale!!.toString()
        val split = value.split(".")
        val notNull = if (split.size > 1) split[1].toIntOrNull()
        else null
        if (notNull != null && notNull == 0)
            return split[0]
        val second = if (notNull == null)
            ""
        else {
            if (split[1].length > 1)
                split[1].substring(0, 2)
            else
                split[1]
        }
        return if (second.isNotEmpty()) "${split[0]},$second" else split[0]
    }

    fun isAllNull(): Boolean {
        return this.initiative == null
                && this.percentageSale == null
                && this.amount == null
                && this.availableSale == null
                && this.milTransactionId == null
                && this.timeStamp == null
    }

    fun voidOp() {
        this.initiative = null
        this.percentageSale = null
        this.amount = null
        this.availableSale = null
        this.milTransactionId = null
        this.timeStamp = null
        this.isCanceledOp = false
    }
}