package it.pagopa.swc.smartpos.app_shared.model.login

import com.google.gson.annotations.SerializedName

data class LoginRefreshTokenRequest(
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("grant_type")
    val grantType: String,
    @SerializedName("refresh_token")
    val refreshToken: String
) : java.io.Serializable