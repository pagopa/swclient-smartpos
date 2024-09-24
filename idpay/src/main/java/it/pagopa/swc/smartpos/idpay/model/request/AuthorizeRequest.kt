package it.pagopa.swc.smartpos.idpay.model.request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AuthorizeRequest(
    @SerializedName("authCodeBlockData") val authCodeBlockData: AuthCodeData
) : Serializable {

    class AuthCodeData(
        @SerializedName("kid") val kid: String?,
        @SerializedName("authCodeBlock") val authCodeBlock: String?,
        @SerializedName("encSessionKey") val encSessionKey: String?
    ) : Serializable {

        override fun toString(): String {
            return "AuthCodeData(kid=$kid, authCodeBlock=$authCodeBlock, encSessionKey=$encSessionKey)"
        }
    }
}

