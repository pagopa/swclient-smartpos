package it.pagopa.swc_smartpos.model.preclose

import com.google.gson.annotations.SerializedName
import it.pagopa.swc_smartpos.model.BaseResponse
import java.io.Serializable

data class PreCloseResponse(
    @SerializedName("Location")
    val location : List<String> = listOf()
) : BaseResponse(), Serializable