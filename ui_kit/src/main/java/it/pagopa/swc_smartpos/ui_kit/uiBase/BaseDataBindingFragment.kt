package it.pagopa.swc_smartpos.ui_kit.uiBase

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.parseAsHtml
import androidx.core.text.toHtml
import androidx.core.text.toSpanned
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import it.pagopa.swc_smartpos.ui_kit.utils.BackPressCallBack
import it.pagopa.swc_smartpos.ui_kit.utils.findActivity
import kotlinx.coroutines.flow.MutableStateFlow

/**This class provides a base fragment implementation for viewBinding
 * @param T Your ViewBinding Class
 * @param Activity Your current activity*/
abstract class BaseDataBindingFragment<T : ViewBinding, Activity : AppCompatActivity> : Fragment() {
    var backPressActionEnabled = MutableStateFlow(true)
    abstract val backPress: () -> Unit
    var mainActivity: Activity? = null
    private var _binding: T? = null
    val binding get() = _binding!!

    /**Use [binding] to implement your [ViewBinding] inflation with ::inflate
     * @sample it.pagopa.swc_smartpos.ui_kit.fragments.BaseFormFragment.viewBinding*/
    abstract fun viewBinding(): (LayoutInflater, ViewGroup?, Boolean) -> T

    /**Override this method to implement all your listeners*/
    open fun setupListeners() {}

    /**Override this method to implement all your LiveData or StateFlow observers*/
    open fun setupObservers() {}

    /**Put into this method your initial ui logic*/
    abstract fun setupUI()

    @CallSuper
    /**Put into this method your final ui logic*/
    open fun setupDestroyView() {
        _binding = null
    }

    @CallSuper
    open fun setupOnResume() {
        mainActivity?.onBackPressedDispatcher?.addCallback(BackPressCallBack {
            if (backPressActionEnabled.value)
                backPress.invoke()
        })
    }

    @CallSuper
    @Suppress("UNCHECKED_CAST")
    open fun setupOnCreate() {
        mainActivity = (activity as? Activity) ?: context?.findActivity() as? Activity
        mainActivity?.onBackPressedDispatcher?.addCallback(BackPressCallBack {
            if (backPressActionEnabled.value)
                backPress.invoke()
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnCreate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = viewBinding().invoke(inflater, container, false)
        setupListeners()
        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onDestroyView() {
        setupDestroyView()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        setupOnResume()
    }

    fun getTextWithArgs(@StringRes id: Int, vararg formatArgs: Any?): CharSequence = getTextSafely(id).toSpanned().toHtml().format(*formatArgs).parseAsHtml()
    fun getTextSafely(@StringRes id: Int) = mainActivity?.resources?.getText(id) ?: ""
    fun getStringSafelyWithOneArg(@StringRes id: Int, arg: String) = mainActivity?.resources?.getString(id, arg).orEmpty()

    fun getStringSafely(id: Int) = mainActivity?.resources?.getString(id).orEmpty()

    /**
     * Waiting kotlin update for an automatic recognition of the generic
     */
    fun binding(bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> T): (LayoutInflater, ViewGroup?, Boolean) -> T {
        return bindingInflater
    }
}