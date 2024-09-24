package it.pagopa.swc_smartpos

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import it.pagopa.swc_smartpos.sharedutils.encryption.decryptText
import it.pagopa.swc_smartpos.sharedutils.encryption.encryptText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStore
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class EncryptionTest {
    private var context: Context? = null

    @Before
    fun before() {
        context = mockk()
    }

    @After
    fun after() {
        context = null
    }


    private fun encryptLongStrings(keyStore: KeyStore, string: String): ArrayList<String?> {
        if (string.length <= 20) return arrayListOf(string.encryptText(context!!, keyStore))
        val list = ArrayList<String>()
        val myX = (string.length / 20f).roundToInt()
        for (i in 0 until 20) {
            val toAdd = i * myX
            list.add(string.substring(toAdd, if (i != 19) toAdd + myX else string.length))
        }
        val encrypted = ArrayList<String?>().apply {
            list.forEach {
                this.add(it.encryptText(context!!, keyStore))
            }
        }
        return encrypted
    }

    private infix fun ArrayList<String?>.decryptWith(keyStore: KeyStore): String {
        val listDecrypt = ArrayList<String>()
        this.forEach {
            listDecrypt.add(it?.decryptText(keyStore).orEmpty())
        }
        val decrypted = StringBuilder()
        listDecrypt.forEach {
            decrypted.append(it)
        }
        return decrypted.toString()
    }

    @Test
    fun testEncryption() {
        val toTest =
            "eyJraWQiOiI1MDNkZTRhOC1mN2NlLTRhNTEtYThiZS00ZWNiZjljZWZiNmUiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJodHRwczovL21pbC1kLWFwaW0uYXp1cmUtYXBpLm5ldC9taWwtaWRwIiwiYXVkIjpbImh0dHBzOi8vbWlsLWQtYXBpbS5henVyZS1hcGkubmV0L21pbC1wYXltZW50LW5vdGljZSIsImh0dHBzOi8vbWlsLWQtYXBpbS5henVyZS1hcGkubmV0L21pbC1mZWUtY2FsY3VsYXRvciJdLCJleHAiOjE2ODA3MDI2MjUsImlhdCI6MTY4MDcwMjMyNSwic2NvcGUiOiJ2ZXJpZnlCeVFyQ29kZSBhY3RpdmF0ZUJ5UXJDb2RlIHZlcmlmeUJ5VGF4Q29kZUFuZE5vdGljZU51bWJlciBhY3RpdmF0ZUJ5VGF4Q29kZUFuZE5vdGljZU51bWJlciBjbG9zZSBnZXRQYXltZW50U3RhdHVzIGdldEZlZSJ9.laNxqs5RPXSzvGJ3IQ5qIY-0jDqj3p3mLiwqnzdk5OG0M54vucsFItBbNMa7knYh2EvCraamil9QeWPFxg3vyn49Z6bYEyHIKYj9wOhGnSqplxVYboWkHOj45zb9cCocGJJdfx6OBwYbBThMx99NuPBMQ_rJt44l_1dszhZTL1P95sQf0MnnJ-UVwkK86cCyyHw2Y5h0MKoIE2t2_q-APWLOhVQrwo9eVGWDxZfj0fUpgjp8KSiPb6fCM2KVy5wHZU5gm40FrUelhWeAKqrfCRTd2hgqWBEobkRNfBuBMxcgLT2fTwY_ijs3-eZKR-kQNE8qDcpWgTR03xasg4-OU2FlHV8Y2DGOkJxASADtSorMoRpIDmRxodteDRCkHCJ1Rgr72CjWXEBQlK1UQdyh4K7bMJm1RHGppH0SL2oItpHH-f7o9cX3uIvbj7O9F-qlc8I0fWXV66JGVmfM76b98UEt6gpnDrwYeLS_51AOLr8pxK-IMusZyiO3CmhmPOsw6dmO8Fz_3uZkisttj8hjhB1CFry-gLIKpsls4jyb9QvxCHykRIw7htcEkBO57s6UkDxwWGY2P4SlmIoYS7Q_TNHqVMsKarRsj_tQViTTGd88ij-EL0RL-Jr5qfmnt9xQ8I_WvyXi2Qoqe5ksrIw_HkJ92xO79XuGtiVH88-RoEo"
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        val encrypted = encryptLongStrings(keyStore, toTest)
        val decrypted = encrypted.decryptWith(keyStore)
        assert(toTest == decrypted)
    }
}