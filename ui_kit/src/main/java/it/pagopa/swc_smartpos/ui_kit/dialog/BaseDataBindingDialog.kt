package it.pagopa.swc_smartpos.ui_kit.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.multidex.BuildConfig
import androidx.viewbinding.ViewBinding
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableInterface
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableManagement

abstract class BaseDataBindingDialog<T : ViewBinding> : DialogFragment(), FoldableInterface {
    private val foldableManagement by lazy {
        FoldableManagement(BuildConfig.DEBUG)
    }

    private var _binding: T? = null
    val binding get() = _binding!!
    protected open var actionFoldable: (() -> Unit)? = null

    /**Put into this method your initial ui logic*/
    abstract fun setupUI()

    /**Override this method to implement all your LiveData or StateFlow observers*/
    open fun setupObservers() {}

    /**Use [binding] to implement your [ViewBinding] inflation with ::inflate
     * @sample it.pagopa.swc_smartpos.ui_kit.fragments.BaseFormFragment.viewBinding*/
    abstract fun viewBinding(): (LayoutInflater, ViewGroup?, Boolean) -> T
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = viewBinding().invoke(inflater, container, false)
        foldableManagement.intoCreateView(this) {
            this.actionFoldable?.invoke()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        foldableManagement.ifViewCreated()
        setupObservers()
        setupUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @CallSuper
    open fun showDialog(manager: FragmentManager?, name: String? = null) {
        runCatching {
            manager?.let {
                this.show(it.beginTransaction().remove(this), name.orEmpty())
            }
        }
    }

    /**
     * Waiting kotlin update for an automatic recognition of the generic
     */
    fun binding(bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T): (LayoutInflater, ViewGroup?, Boolean) -> T {
        return bindingInflater
    }
}