package it.pagopa.swc_smartpos.ui_kit.utils

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowInfoTracker
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseFormFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

data class ListViewItem(val view: View) : Serializable {
    val isViewGroup by lazy { view is ViewGroup }
    var used = false
}

interface FoldableInterface {
    @RequiresApi(Build.VERSION_CODES.Q)
    fun collectFoldableState(activity: AppCompatActivity, onCollected: (FoldableState) -> Unit) {
        activity.lifecycleScope.launch(Dispatchers.Main) {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity).collect {
                        onCollected.invoke(it.toFoldableState())
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun collectFoldableState(fragment: Fragment, onCollected: (FoldableState) -> Unit) {
        fragment.lifecycleScope.launch(Dispatchers.Main) {
            fragment.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                WindowInfoTracker.getOrCreate(fragment.requireActivity())
                    .windowLayoutInfo(fragment.requireActivity()).collect {
                        onCollected.invoke(it.toFoldableState())
                    }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun updateView(view: View, inflateId: Int) {
        if (inflateId == 0) return
        val newView = LayoutInflater.from(view.context).inflate(inflateId, null)
        val listView = (newView to view).fillListView(view.context)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childOld = view.getChildAt(i)
                childOld.updateViewInternal(listView)
            }
            view.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun updateView(fragment: Fragment, view: View, inflateId: Int) {
        if (inflateId == 0) return
        val newView = LayoutInflater.from(fragment.context).inflate(inflateId, null)
        val listView = (newView to view).fillListView(fragment.context)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childOld = view.getChildAt(i)
                childOld.updateViewInternal(listView)
            }
            if (fragment is BaseFormFragment<*>)
                fragment.updateLLInputs()
            view.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun Pair<View, View>.fillListView(context: Context?): ArrayList<ListViewItem> {
        val listView = ArrayList<ListViewItem>()
        fun View.reElaborate(oldV: View, mTag: String) {
            oldV.tag = mTag
            this.tag = mTag
            val item = ListViewItem(this)
            listView.add(item)
            if (item.isViewGroup)
                listView.addAll((item.view to oldV).fillListView(context))
        }
        val (newView, oldView) = this
        if (newView is ViewGroup) {
            if (newView is RecyclerView) {
                val oldViewRv = oldView as RecyclerView
                for (i in 0 until oldViewRv.childCount) {
                    val oldViewChild = oldViewRv.getChildAt(i)
                    val itemView = LayoutInflater.from(context).inflate(oldViewChild.sourceLayoutResId, null)
                    itemView.layoutParams = oldViewChild.layoutParams
                    newView.addView(itemView)
                    val myTag = "$i:${oldViewChild.javaClass.name}: ${oldViewChild.id}"
                    itemView.reElaborate(oldViewChild, myTag)
                }
            } else {
                for (i in 0 until newView.childCount) {
                    val child = newView.getChildAt(i)
                    val myTag = "$i:${child.javaClass.name}: ${child.id}"
                    val oldViewChild = (oldView as ViewGroup).getChildAt(i)
                    child.reElaborate(oldViewChild, myTag)
                }
            }
        }
        return listView
    }

    private fun View.updateViewInternal(listView: ArrayList<ListViewItem>) {
        for (i in 0 until listView.size) {
            if (listView[i].view.tag == this.tag && !listView[i].used) {
                listView[i].used = true
                val newView = listView[i].view
                this.layoutParams = newView.layoutParams
                this.setPadding(newView.paddingStart, newView.paddingTop, newView.paddingEnd, newView.paddingBottom)
                when (this) {
                    is TextView -> this.setTextSize(TypedValue.COMPLEX_UNIT_PX, (listView[i].view as TextView).textSize)
                    is ViewGroup -> {
                        for (j in 0 until this.childCount)
                            this.getChildAt(j).updateViewInternal(listView)
                    }
                }
                break
            }
        }
    }
}