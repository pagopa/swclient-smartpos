package it.pagopa.swc_smartpos.model

import com.google.gson.annotations.SerializedName

/**Every model class which would like to use [it.pagopa.swc_smartpos.network.NetworkObserver] must extend this class.
 * Basically every network response will bring outcome ("ok" or not)
 * @sample [QrCodeVerifyResponse]*/
abstract class BaseResponse {
    @SerializedName("outcome")
    val outcome: String? = null
    override fun toString(): String {
        return "outcome:${outcome.orEmpty()}"
    }
}