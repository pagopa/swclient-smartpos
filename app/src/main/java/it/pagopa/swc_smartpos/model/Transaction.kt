package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment

data class Transaction(
    @SerializedName("acquirerId")
    val acquirerId: String? = null,
    @SerializedName("channel")
    val channel: String? = null,
    @SerializedName("fee")
    val fee: Int? = null,
    @SerializedName("insertTimestamp")
    val insertTimestamp: String? = null,
    @SerializedName("merchantId")
    val merchantId: String? = null,
    @SerializedName("notices")
    val notices: List<Notice>? = null,
    @SerializedName("status")
    val status: String,
    @SerializedName("terminalId")
    val terminalId: String? = null,
    @SerializedName("totalAmount")
    val totalAmount: Int? = null,
    @SerializedName("transactionId")
    val transactionId: String? = null
) : java.io.Serializable {
    fun amountPlusFee(): Int? {
        if (this.totalAmount == null)
            return null
        if (this.fee == null)
            return null
        return this.totalAmount + this.fee
    }

    fun getStato(): Status {
        return Status.valueOf(status)
    }
}


enum class Status(val status: String, val state: BaseResultFragment.State?) {
    PRE_CLOSE("PRE_CLOSE", BaseResultFragment.State.Info),
    PENDING("PENDING", BaseResultFragment.State.Info),
    ERROR_ON_CLOSE("ERROR_ON_CLOSE", BaseResultFragment.State.Warning),
    CLOSED("CLOSED", BaseResultFragment.State.Success),
    ERROR_ON_RESULT("ERROR_ON_RESULT", BaseResultFragment.State.Warning),
    ERROR_ON_PAYMENT("ERROR_ON_PAYMENT", BaseResultFragment.State.Error),
    ABORT("ABORT", null),
    RIMBORSATA("RIMBORSATA", null) // STATO ANCORA DA DECIDERE
}