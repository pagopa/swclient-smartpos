import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import it.pagopa.swc.smartpos.app_shared.Mockable
import it.pagopa.swc_smartpos.sharedutils.Event
import it.pagopa.swc_smartpos.sharedutils.extensions.Token
import it.pagopa.swc_smartpos.sharedutils.interfaces.PrintStatus
import it.pagopa.swc_smartpos.sharedutils.interfaces.SecondScreenConnection
import it.pagopa.swc_smartpos.sharedutils.interfaces.ServiceConnected
import it.pagopa.swc_smartpos.sharedutils.model.Business
import it.pagopa.swc_smartpos.sharedutils.model.Payment
import it.pagpa.swc_smartpos.android.AndroidWrapper
import java.util.UUID

@Mockable
class SdkUtility(private val act: AppCompatActivity, private val isMock: Boolean) {
    private val mainClass = AndroidWrapper()
    fun getWrapperClass(): Any = Any()

    //Class written just to let compile AndroidNative
    data class FakeToken(val accessToken: Token? = null)

    //nothing to do here
    fun registerBusinessLauncher() {
    }

    //nothing to do here
    fun launchBusinessInfo() {
    }

    //fun getCurrentAccount() = mainClass.currentAccount
    fun getCurrentBusiness() = MutableLiveData(
        Business(
            UUID.randomUUID().toString(),
            "30390022",
            "12346789",
            listOf("4585625"),
            "12345678901"
        )
    )

    fun getToken() = MutableLiveData(Event(FakeToken()))
    fun onUserConnected(onUserConnected: ServiceConnected) {
    }

    fun onBusinessConnected(onBusinessConnected: ServiceConnected) {
    }

    fun onSecondScreenConnected(onSecondScreenConnected: SecondScreenConnection) {
    }

    fun askForToken() {
    }

    fun getCurrentUser() {
    }

    fun refreshCurrentBusiness() {
    }

    fun registerPaymentLauncher(whatToDo: (Payment) -> Unit) {
    }

    fun launchPayment(amount: Long, issuer: String) {
    }

    fun bindPrinterService(onPrinterConnected: ServiceConnected) {
    }

    fun bindCardReaderService(onCardReaderConnected: ServiceConnected) {
    }

    fun printDrawable(context: Context, drawable: Drawable, height: Int, status: PrintStatus) {
        mainClass.printDrawable(context, act, drawable, height, status)
    }

    fun displayDrawable(@DrawableRes backgroundImage: Int) {
    }

    fun displayDrawable(drawable: Drawable?) {
    }

    fun resetWelcomeScreen() {
    }

    fun unBindServices() {
    }

    fun unBindCardReaderService() {
    }
}