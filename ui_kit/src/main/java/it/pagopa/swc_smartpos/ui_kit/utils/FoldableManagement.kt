package it.pagopa.swc_smartpos.ui_kit.utils

import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import java.io.Serializable

class FoldableManagement(private val enableLogs: Boolean = false) : FoldableInterface, Serializable {
    private var currentFoldableState = FoldableState.NotAFoldable
    private var canChange = false
    private var isFirstTime = true
    fun intoCreateView(activity: AppCompatActivity, onPhoneChanging: (value: FoldableState) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.collectFoldableState(activity) {
                if (canChange) {
                    currentFoldableState = if (isFirstTime)
                        it
                    else {
                        if (enableLogs) {
                            Log.i("currentFoldableState", currentFoldableState.name)
                            Log.i("collected", it.name)
                        }
                        if (currentFoldableState != it)
                            onPhoneChanging.invoke(it)
                        it
                    }
                    isFirstTime = false
                }
            }
        }
    }

    fun intoCreateView(fragment: Fragment, onPhoneChanging: (newValue: FoldableState) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.collectFoldableState(fragment) {
                if (canChange) {
                    currentFoldableState = if (isFirstTime)
                        it
                    else {
                        if (enableLogs) {
                            Log.i("currentFoldableState", currentFoldableState.name)
                            Log.i("collected", it.name)
                        }
                        if (currentFoldableState != it)
                            onPhoneChanging.invoke(it)
                        it
                    }
                    isFirstTime = false
                }
            }
        }
    }

    fun ifViewCreated() {
        canChange = true
    }
}