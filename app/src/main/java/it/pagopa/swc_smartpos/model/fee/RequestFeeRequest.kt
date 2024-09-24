package it.pagopa.swc_smartpos.model.fee

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.Transfer

data class RequestFeeRequest(
    @SerializedName("notices")
    val notices: List<Notice>,
    @SerializedName("paymentMethod")
    val paymentMethod: String = "PAYMENT_CARD"
) : java.io.Serializable {
    data class Notice(
        @SerializedName("amount")
        val amount: Int?,
        @SerializedName("paTaxCode")
        val paTaxCode: String?,
        @SerializedName("transfers")
        val transfers: List<Transfer>?
    ) : java.io.Serializable
}