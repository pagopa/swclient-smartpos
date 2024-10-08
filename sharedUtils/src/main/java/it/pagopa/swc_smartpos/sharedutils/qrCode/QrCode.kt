package it.pagopa.swc_smartpos.sharedutils.qrCode

//PAGOPA|002|305000005457917604|09633951000|11741 OWNER|VERSIONE|CODICE_AVVISO|CODICE_FISCALE_ENTE|IMPORTO (virgola prima ultime due cifre)
/**Collaudo: PAGOPA|002|302051234567890124|77777777777|9999
const val NOTICE_GLITCH = "NOTICE_GLITCH"
* NOTICE_GLITCH: PAGOPA|002|302051234567890124|00000000001|9999
* WRONG_NOTICE_DATA: PAGOPA|002|302051234567890124|00000000002|9999
* CREDITOR_PROBLEMS: PAGOPA|002|302051234567890124|00000000003|9999
* PAYMENT_ALREADY_IN_PROGRESS: PAGOPA|002|302051234567890124|00000000004|9999
* EXPIRED_NOTICE: PAGOPA|002|302051234567890124|00000000005|9999
* UNKNOWN_NOTICE: PAGOPA|002|302051234567890124|00000000006|9999
* REVOKED_NOTICE: PAGOPA|002|302051234567890124|00000000007|9999
* NOTICE_ALREADY_PAID: PAGOPA|002|302051234567890124|00000000008|9999
* UNEXPECTED_ERROR: PAGOPA|002|302051234567890124|00000000009|9999
* with 101,102 etc. the error will occur during activate
 * CORRECT: PAGOPA|002|302051234567890124|00000000201|9999*/
data class QrCode(val code: String, val format: String?) : java.io.Serializable
