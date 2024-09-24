package it.pagopa.swc_smartpos.model.preclose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PreCloseRequest(
    @SerializedName("fee")
    val fee: Int,
    @SerializedName("outcome")
    val outcome: String,
    @SerializedName("paymentTokens")
    val paymentTokens: List<String>,
    @SerializedName("totalAmount")
    val totalAmount: Int,
    @SerializedName("transactionId")
    val transactionId: String,
    @SerializedName("preset")
    val preset : PresetRequest ? = null
) : Serializable{

    data class PresetRequest(
        @SerializedName("paTaxCode")
        val paTaxCode : String,
        @SerializedName("subscriberId")
        val subcriberId : String,
        @SerializedName("presetId")
        val presetId : String,

    ) : Serializable
}




