package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.sharedutils.extensions.toAmountFormatted

data class QrCodeVerifyResponse(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("company")
    val company: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("paTaxCode")
    val paTaxCode: String,
    @SerializedName("noticeNumber")
    val noticeNumber: String,
    @SerializedName("note")
    val note: String,
    @SerializedName("office")
    val office: String
) : BaseResponse(), java.io.Serializable {
    var originalCode = ""
    fun amountFormatted() = amount.toAmountFormatted()
}