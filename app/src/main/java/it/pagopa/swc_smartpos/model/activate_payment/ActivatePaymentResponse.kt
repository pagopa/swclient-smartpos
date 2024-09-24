package it.pagopa.swc_smartpos.model.activate_payment

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.BaseResponse
import it.pagopa.swc_smartpos.model.Transfer

data class ActivatePaymentResponse(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("paTaxCode")
    val paTaxCode: String,
    @SerializedName("paymentToken")
    val paymentToken: String,
    @SerializedName("transfers")
    val transfers: List<Transfer>
) : BaseResponse(), java.io.Serializable