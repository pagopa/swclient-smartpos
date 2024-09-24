package it.pagopa.swc.smartpos.idpay.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import it.pagopa.swc.smartpos.idpay.MainActivity
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.DialogTroubleWithQrBinding
import it.pagopa.swc.smartpos.idpay.second_screen.showInsertTrxCodeSecondScreen
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableInterface
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity

class DialogTroubleWithQr : DialogFragment(), FoldableInterface {
    private var _binding: DialogTroubleWithQrBinding? = null
    val binding get() = _binding!!
    private val mainActivity by lazy {
        context.findActivity() as? MainActivity
    }
    private var trxCode: String? = null
    var onDismiss: (() -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(requireActivity()).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(LinearLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        })
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            decorView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null)
            trxCode = savedInstanceState.getString(trxCodeKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTroubleWithQrBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivity?.onUpdatingViewForDialog = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                this.updateView(this, binding.root, R.layout.dialog_trouble_with_qr)
        }
        binding.tvTrxCode.text = trxCode.orEmpty()
        mainActivity?.showInsertTrxCodeSecondScreen(trxCode)
        binding.ivCloseDialog.setOnClickListener {
            onDismiss?.invoke()
            this.dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(trxCodeKey, trxCode)
        super.onSaveInstanceState(outState)
    }

    fun show(supportFragmentManager: FragmentManager?) {
        supportFragmentManager?.let {
            this.show(it, this.javaClass.name)
        }
    }

    companion object {
        const val trxCodeKey = "trxCodeKey"
        fun newInstance(trxCode: String?, onDismiss: () -> Unit) = DialogTroubleWithQr().apply {
            this.trxCode = trxCode
            this.onDismiss = onDismiss
        }
    }
}