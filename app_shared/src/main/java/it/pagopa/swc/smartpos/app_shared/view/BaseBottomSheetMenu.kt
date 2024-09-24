package it.pagopa.swc.smartpos.app_shared.view

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import it.pagopa.swc.smartpos.app_shared.BaseMainActivity
import it.pagopa.swc.smartpos.app_shared.R
import it.pagopa.swc.smartpos.app_shared.databinding.BottomSheetMenuBinding
import it.pagopa.swc.smartpos.app_shared.databinding.ItemMenuBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.ui_kit.utils.FoldableInterface
import it.pagopa.swc_smartpos.ui_kit.utils.disableScroll
import it.pagopa.swc_smart_pos.ui_kit.R as RUiKit

abstract class BaseBottomSheetMenu(
    private val activity: BaseMainActivity<*>
) : BottomSheetDialog(activity, RUiKit.style.SheetDialog), FoldableInterface {
    abstract val isPoynt: Boolean
    abstract val listItemMenu: List<ItemMenu>

    fun showMenu() {
        val bindingBottomSheet = BottomSheetMenuBinding.inflate(activity.layoutInflater)
        this.setContentView(bindingBottomSheet.root)
        bindingBottomSheet.rvItems.adapter = AdapterMenu(listItemMenu)
        bindingBottomSheet.rvItems.disableScroll()
        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        bindingBottomSheet.llLine.setOnClickListener {
            this.dismiss()
        }
        if (isPoynt)
            bindingBottomSheet.llLogout.isVisible = false
        else {
            bindingBottomSheet.llLogout.setOnClickListener {
                this.dismiss()
                activity.backToLogin()
            }
        }
        activity.onUpdatingViewForDialog = {
            if (this.isShowing) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    this.updateView(bindingBottomSheet.root, R.layout.bottom_sheet_menu)
            }
        }
        this.show()
    }

    data class ItemMenu(@StringRes val title: Int, @DrawableRes val icon: Int, val action: () -> Unit)

    inner class AdapterMenu(val list: List<ItemMenu>) : BaseRecyclerView<ItemMenu, ItemMenuBinding>(list) {
        override fun viewBinding() = binding(ItemMenuBinding::inflate)
        override fun bind(context: Context, item: ItemMenu, pos: Int, binding: ItemMenuBinding) {
            binding.icon.setImageDrawable(AppCompatResources.getDrawable(context, item.icon))
            binding.title.text = context.resources.getText(item.title)
            binding.line.isVisible = pos != list.size - 1
            binding.ll.setOnClickListener {
                this@BaseBottomSheetMenu.dismiss()
                item.action.invoke()
            }
        }
    }
}