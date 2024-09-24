package it.pagopa.swc_smartpos.sharedutils.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import it.pagopa.swc_smartpos.sharedUtils.R
import it.pagopa.swc_smartpos.sharedutils.interfaces.BackPressCallBack
import it.pagopa.swc_smartpos.sharedutils.qrCode.QrCode

abstract class CustomScannerFragment : Fragment() {
    abstract val backPress: () -> Unit
    abstract val closeAction: () -> Unit
    abstract val actionScanned: (QrCode) -> Unit
    private var barcodeScannerView: CompoundBarcodeView? = null
    @CallSuper
    open fun setupOnCreate(){
        activity?.onBackPressedDispatcher?.addCallback(BackPressCallBack { backPress.invoke() })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnCreate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_custom_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeScannerView = view.findViewById(R.id.zxing_barcode_scanner)
        val beepManager = BeepManager(activity)
        barcodeScannerView?.decodeContinuous {
            barcodeScannerView?.pause()
            beepManager.playBeepSoundAndVibrate()
            actionScanned.invoke(QrCode(it.text, it.barcodeFormat.name))
        }
        barcodeScannerView?.findViewById<AppCompatImageView>(R.id.close_btn)?.setOnClickListener {
            closeAction.invoke()
        }
    }

    fun resumeScannerView() {
        barcodeScannerView?.resume()
    }

    override fun onResume() {
        super.onResume()
        resumeScannerView()
        activity?.onBackPressedDispatcher?.addCallback(BackPressCallBack { backPress.invoke() })
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView?.pause()
    }
}