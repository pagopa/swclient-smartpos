package it.pagopa.swc_smartpos.model.close_payment

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.BaseResponse

data class ClosePaymentResponse(
    @SerializedName("Location")
    val location: List<String>?,
    @SerializedName("retry-after")
    val retryAfter: List<Int>?,
    @SerializedName("max-retries")
    val maxRetries: List<Int>?
) : BaseResponse(), java.io.Serializable
