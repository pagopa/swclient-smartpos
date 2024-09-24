package it.pagopa.swc_smartpos.sharedutils.encryption

import android.content.Context
import androidx.annotation.CheckResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.security.KeyStore
import kotlin.math.roundToInt

@CheckResult
fun String.encryptText(context: Context?, keyStore: KeyStore): String? {
    if (context == null) return this
    return EncryptionFromApi18().encrypt(context, this, keyStore)
}

@CheckResult
fun String.decryptText(keyStore: KeyStore): String? {
    return EncryptionFromApi18().decrypt(this, keyStore)
}

fun ArrayList<String?>?.decryptWith(keyStore: KeyStore, onDecrypted: (String) -> Unit) {
    if (this == null) {
        onDecrypted.invoke("")
        return
    }
    var decryptJob: Job? = null
    decryptJob = CoroutineScope(Dispatchers.Default).launch {
        val listDecrypt = ArrayList<String>()
        this@decryptWith.forEach {
            listDecrypt.add(it?.decryptText(keyStore).orEmpty())
        }
        val decrypted = StringBuilder()
        listDecrypt.forEach {
            decrypted.append(it)
        }
        onDecrypted.invoke(decrypted.toString())
        decryptJob?.cancel()
    }
    decryptJob.start()
}

fun Context?.encryptLongStrings(keyStore: KeyStore, string: String, encryptedList: (ArrayList<String?>) -> Unit) {
    var encryptJob: Job? = null
    encryptJob = CoroutineScope(Dispatchers.Default).launch {
        if (string.length <= 20) {
            encryptedList.invoke(arrayListOf(string.encryptText(this@encryptLongStrings, keyStore)))
        } else {
            val list = ArrayList<String>()
            val myX = (string.length / 20f).roundToInt()
            for (i in 0 until 20) {
                val toAdd = i * myX
                list.add(string.substring(toAdd, if (i != 19) toAdd + myX else string.length))
            }
            val encrypted = ArrayList<String?>().apply {
                list.forEach { string ->
                    this.add(string.encryptText(this@encryptLongStrings, keyStore))
                }
            }
            encryptedList.invoke(encrypted)
            encryptJob?.cancel()
        }
    }
    encryptJob.start()
}