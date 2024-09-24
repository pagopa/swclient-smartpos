package it.pagopa.swc_smartpos.ui_kit.uiBase

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseRecyclerView<T, VB : ViewBinding>(
    private val list: List<T>
) : RecyclerView.Adapter<BaseRecyclerView<T, VB>.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            parent.context,
            viewBinding().invoke(LayoutInflater.from(parent.context), parent, false)
        )
    }

    abstract fun viewBinding(): (LayoutInflater, ViewGroup?, Boolean) -> VB
    fun binding(bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB): (LayoutInflater, ViewGroup?, Boolean) -> VB {
        return bindingInflater
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], position)
    }

    override fun getItemCount(): Int = list.size
    abstract fun bind(context: Context, item: T, pos: Int, binding: VB)
    inner class ViewHolder(val context: Context, private val vb: VB) :
        RecyclerView.ViewHolder(vb.root) {
        fun bind(item: T, pos: Int) {
            this@BaseRecyclerView.bind(context, item, pos, vb)
        }
    }
}


abstract class BaseRecyclerViewStableIds<T, VB : ViewBinding>(list: List<T>) : BaseRecyclerView<T, VB>(list) {
    override fun getItemId(position: Int): Long = position.toLong()
    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }
}