package it.pagopa.swc_smartpos.model.fee

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.BaseResponse

data class RequestFeeResponse(
    @SerializedName("fee")
    val fee: Int
) : BaseResponse(), java.io.Serializable