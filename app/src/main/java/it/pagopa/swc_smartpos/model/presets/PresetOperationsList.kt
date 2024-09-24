package it.pagopa.swc_smartpos.model.presets


import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.Notice
import it.pagopa.swc_smartpos.model.QrCodeVerifyResponse
import java.io.Serializable

data class PresetOperationsList(
    @SerializedName("presets")
    val presets: List<Preset>?
) : Serializable {
    data class Preset(
        @SerializedName("creationTimestamp")
        val creationTimestamp: String,
        @SerializedName("noticeNumber")
        val noticeNumber: String,
        @SerializedName("noticeTaxCode")
        val noticeTaxCode: String,
        @SerializedName("operationType")
        val operationType: String,
        @SerializedName("paTaxCode")
        val paTaxCode: String,
        @SerializedName("presetId")
        val presetId: String,
        @SerializedName("status")
        val status: String,
        @SerializedName("statusDetails")
        val statusDetails: StatusDetails? = null,
        @SerializedName("statusTimestamp")
        val statusTimestamp: String,
        @SerializedName("subscriberId")
        val subscriberId: String
    ) : Serializable {

        fun toQrCodeVerifyResponse(): QrCodeVerifyResponse {
            val firstNotice = this.statusDetails?.notices?.getOrNull(0)
            return QrCodeVerifyResponse(
                this.statusDetails?.totalAmount ?: 0 ,
                firstNotice?.company.orEmpty(),
                firstNotice?.description.orEmpty(),
                firstNotice?.paTaxCode.orEmpty(),
                firstNotice?.noticeNumber.orEmpty(),
                firstNotice?.description.orEmpty(),
                firstNotice?.office.orEmpty()
            )
        }

        data class StatusDetails(
            @SerializedName("acquirerId")
            val acquirerId: String,
            @SerializedName("channel")
            val channel: String,
            @SerializedName("fee")
            val fee: Int,
            @SerializedName("insertTimestamp")
            val insertTimestamp: String,
            @SerializedName("merchantId")
            val merchantId: String,
            @SerializedName("notices")
            val notices: List<Notice>,
            @SerializedName("status")
            val status: String,
            @SerializedName("terminalId")
            val terminalId: String,
            @SerializedName("totalAmount")
            val totalAmount: Int,
            @SerializedName("transactionId")
            val transactionId: String
        ) : Serializable
    }
}