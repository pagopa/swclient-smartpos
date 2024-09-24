package it.pagopa.swc_smartpos.network

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

    object ErrorCode {
        const val NOTICE_GLITCH = "NOTICE_GLITCH"
        const val WRONG_NOTICE_DATA = "WRONG_NOTICE_DATA"
        const val CREDITOR_PROBLEMS = "CREDITOR_PROBLEMS"
        const val PAYMENT_ALREADY_IN_PROGRESS = "PAYMENT_ALREADY_IN_PROGRESS"
        const val EXPIRED_NOTICE = "EXPIRED_NOTICE"
        const val UNKNOWN_NOTICE = "UNKNOWN_NOTICE"
        const val REVOKED_NOTICE = "REVOKED_NOTICE"
        const val NOTICE_ALREADY_PAID = "NOTICE_ALREADY_PAID"
    }
}