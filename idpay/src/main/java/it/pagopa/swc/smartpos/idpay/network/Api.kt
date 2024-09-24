package it.pagopa.swc.smartpos.idpay.network

import it.pagopa.swc_smartpos.network.connection.Connection
import java.util.UUID

object Api {
    fun loginHeader(
        acquirerId: String,
        terminalId: String,
        merchantId: String
    ): Connection.CustomUrlConnection = Connection.CustomUrlConnection(
        contentType = "application/x-www-form-urlencoded",
        customHeader = mapOf(
            "RequestId" to UUID.randomUUID().toString(),
            "AcquirerId" to acquirerId,
            "Channel" to "POS",
            "TerminalId" to terminalId,
            "MerchantId" to merchantId,
        )
    )

    fun header(
        bearer: String,
        acquirerId: String,
        terminalId: String,
        merchantId: String,
        forRefreshToken: Boolean = false,
    ): Connection.CustomUrlConnection = Connection.CustomUrlConnection(
        contentType = if (forRefreshToken) "application/x-www-form-urlencoded" else "application/json;charset=UTF-8",
        bearer = bearer,
        customHeader = mapOf(
            "RequestId" to UUID.randomUUID().toString(),
            "AcquirerId" to acquirerId,
            "Channel" to "POS",
            "TerminalId" to terminalId,
            "MerchantId" to merchantId,
        )
    )
}