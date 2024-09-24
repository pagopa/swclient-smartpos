package it.pagopa.swc_smartpos.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import it.pagopa.swc_smartpos.R
import it.pagopa.swc_smartpos.databinding.ItemUiKitShowCaseBinding
import it.pagopa.swc_smartpos.databinding.UiKitShowCaseBinding
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc_smartpos.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitDialog
import it.pagopa.swc_smartpos.ui_kit.dialog.UiKitStyledDialog
import it.pagopa.swc_smartpos.ui_kit.fragments.BaseResultFragment
import it.pagopa.swc_smartpos.ui_kit.toast.UiKitToast
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerView
import it.pagopa.swc_smartpos.ui_kit.utils.CustomBtnCustomizer
import it.pagopa.swc_smartpos.ui_kit.utils.Style
import it.pagopa.swc_smartpos.ui_kit.utils.setupUiKitToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import it.pagopa.swc_smart_pos.ui_kit.R as RUikit

class UiKitShowCase : BaseDataBindingFragmentApp<UiKitShowCaseBinding>() {
    private val _data = MutableLiveData<UiKitToast?>(null)
    val data: LiveData<UiKitToast?> = _data
    override val backPress: Action get() = { findNavController().navigateUp() }
    override val header: HeaderView
        get() = HeaderView(
            HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
            HeaderView.HeaderString(R.string.ui_kit_show_case, R.color.primary),
            HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary, backPress),
            R.color.white
        )

    override fun viewBinding() = binding(UiKitShowCaseBinding::inflate)
    private val list = Cases.toUiKitCases()

    override fun setupUI() {
        binding.rv.adapter = Adapter(list)
        binding.root.setupUiKitToast(viewLifecycleOwner, _data, data)
    }

    private inner class Adapter(list: List<UiKitCases>) : BaseRecyclerView<UiKitCases, ItemUiKitShowCaseBinding>(list) {
        private val dialogsTitle = "Test title"
        private val dialogsDescription = "Lorem ipsum dolor sit amet, consectetur adipiscing elit"
        private val styledDialogsMainBtnText = "Show Lazy loading"
        private val dialogsSecondaryBtnText = "Test secondary btn"
        private val secondaryBtnToastText = "You clicked secondary btn"
        override fun viewBinding() = binding(ItemUiKitShowCaseBinding::inflate)
        override fun bind(context: Context, item: UiKitCases, pos: Int, binding: ItemUiKitShowCaseBinding) {
            binding.tv.text = item.name
            when (item.value) {
                Cases.Buttons -> binding.root.setOnClickListener {
                    this@UiKitShowCase.navigate(R.id.action_uiKitShowCase_to_buttonsShowCase)
                }

                Cases.Dialog -> binding.root.setOnClickListener {
                    val dialog = UiKitDialog.withTitle(dialogsTitle)
                        .withDescription(dialogsDescription)
                        .withCloseVisible()
                        .withMainBtn("Test button text") {
                            Toast.makeText(context, "You clicked main btn", Toast.LENGTH_SHORT).show()
                        }.withSecondaryBtn(dialogsSecondaryBtnText) {
                            Toast.makeText(context, secondaryBtnToastText, Toast.LENGTH_SHORT).show()
                        }.withMainCustomBtn(CustomBtnCustomizer(
                            getTextSafely(R.string.ui_kit_show_case),
                            RUikit.drawable.arrow_right, true, this@UiKitShowCase
                        ) {
                            Toast.makeText(context, "CLicked custom button", Toast.LENGTH_LONG).show()
                        }).withSecondaryCustomBtn(CustomBtnCustomizer(
                            getTextSafely(R.string.ui_kit_show_case),
                            RUikit.drawable.home_primary, false, this@UiKitShowCase
                        ) {
                            Toast.makeText(context, "CLicked custom button second", Toast.LENGTH_LONG).show()
                        })
                    dialog.dismissOnMainBtnClick = false
                    dialog.dismissOnSecondaryBtnClick = false
                    dialog.showDialog(mainActivity?.supportFragmentManager)
                }

                Cases.StyledDialog -> binding.root.setOnClickListener {
                    val dialog = UiKitStyledDialog
                        .withTitle(dialogsTitle)
                        .withDescription(dialogsDescription)
                    dialog.withMainBtn(styledDialogsMainBtnText) {
                        dialog.loading(true)
                        CoroutineScope(Dispatchers.Default).launch {
                            delay(3000L)
                            dialog.loading(false)
                        }
                    }.withMainCustomBtn(CustomBtnCustomizer(
                        getTextSafely(R.string.ui_kit_show_case),
                        RUikit.drawable.arrow_right, false, this@UiKitShowCase
                    ) {
                        Toast.makeText(context, "CLicked custom button", Toast.LENGTH_LONG).show()
                    }).withSecondaryCustomBtn(CustomBtnCustomizer(
                        getTextSafely(R.string.ui_kit_show_case),
                        RUikit.drawable.home_primary, true, this@UiKitShowCase
                    ) {
                        Toast.makeText(context, "CLicked custom button second", Toast.LENGTH_LONG).show()
                    })
                    dialog.withSecondaryBtn(dialogsSecondaryBtnText) {
                        Toast.makeText(context, secondaryBtnToastText, Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismissOnMainBtnClick = false
                    dialog.dismissOnSecondaryBtnClick = false
                    createPopUpMenu(binding.root, arrayOf(Pair(Style.Success.name) {
                        dialog.withStyle(Style.Success)
                        dialog.showDialog(mainActivity?.supportFragmentManager)
                    }, Pair(Style.Info.name) {
                        dialog.withStyle(Style.Info)
                        dialog.showDialog(mainActivity?.supportFragmentManager)
                    }, Pair(Style.Warning.name) {
                        dialog.withStyle(Style.Warning)
                        dialog.showDialog(mainActivity?.supportFragmentManager)
                    }, Pair(Style.Error.name) {
                        dialog.withStyle(Style.Error)
                        dialog.showDialog(mainActivity?.supportFragmentManager)
                    })).show()
                }

                Cases.Result -> binding.root.setOnClickListener {
                    createPopUpMenu(binding.root, arrayOf(Pair(BaseResultFragment.State.Success.name) {
                        navigate(R.id.action_uiKitShowCase_to_resultFragment, Bundle().apply {
                            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Success)
                            this.putInt(BaseResultFragment.titleArg, R.string.title_paymentCompleted)
                            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer("test Success", R.drawable.arrow_back, true) {
                                it.findNavController().navigateUp()
                            })
                            putBoolean(uiKitRecognition, true)
                        })
                    }, Pair(BaseResultFragment.State.Info.name) {
                        navigate(R.id.action_uiKitShowCase_to_resultFragment, Bundle().apply {
                            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Info)
                            this.putInt(BaseResultFragment.titleArg, R.string.title_secondScreen_uncertainOutcome)
                            this.putInt(BaseResultFragment.descriptionArg, R.string.info_op_description)
                            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer("test Info", R.drawable.no_receipt, true) {
                                it.findNavController().navigateUp()
                            })
                            this.putSerializable(BaseResultFragment.secondButtonArg, CustomBtnCustomizer("test Info", R.drawable.mail, false) {
                                it.findNavController().navigateUp()
                            })
                            putBoolean(uiKitRecognition, true)
                        })
                    }, Pair(BaseResultFragment.State.Error.name) {
                        navigate(R.id.action_uiKitShowCase_to_resultFragment, Bundle().apply {
                            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Error)
                            this.putInt(BaseResultFragment.titleArg, R.string.title_transactionCancelled)
                            this.putInt(BaseResultFragment.descriptionArg, R.string.error_op_description)
                            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer("test Error", R.drawable.mail_white, true) {
                                it.findNavController().navigateUp()
                            })
                            this.putSerializable(BaseResultFragment.secondButtonArg, CustomBtnCustomizer("test Error", R.drawable.mail, false) {
                                it.findNavController().navigateUp()
                            })
                            putBoolean(uiKitRecognition, true)
                        })
                    }, Pair(BaseResultFragment.State.Warning.name) {
                        navigate(R.id.action_uiKitShowCase_to_resultFragment, Bundle().apply {
                            this.putSerializable(BaseResultFragment.stateArg, BaseResultFragment.State.Warning)
                            this.putInt(BaseResultFragment.titleArg, R.string.title_unknownNotice)
                            this.putInt(BaseResultFragment.descriptionArg, R.string.paragraph_contactSupport)
                            this.putSerializable(BaseResultFragment.firstButtonArg, CustomBtnCustomizer("test Warning", R.drawable.mail_white, true) {
                                it.findNavController().navigateUp()
                            })
                            this.putSerializable(BaseResultFragment.secondButtonArg, CustomBtnCustomizer("test Warning", R.drawable.print_warning_dark, true) {
                                it.findNavController().navigateUp()
                            })
                            putBoolean(uiKitRecognition, true)
                        })
                    })).show()
                }

                Cases.Receipt -> binding.root.setOnClickListener {
                    navigate(R.id.action_uiKitShowCase_to_receiptFragment, Bundle().apply {
                        putBoolean(uiKitRecognition, true)
                    })
                }

                Cases.Outro -> binding.root.setOnClickListener {
                    navigate(R.id.action_uiKitShowCase_to_outroFragment, Bundle().apply {
                        putBoolean(uiKitRecognition, true)
                    })
                }

                Cases.Input -> binding.root.setOnClickListener {
                    navigate(R.id.action_uiKitShowCase_to_inputFieldShowCase)
                }

                Cases.Toast -> binding.root.setOnClickListener {
                    createPopUpMenu(binding.root, arrayOf(Pair(UiKitToast.Value.Generic.name) {
                        _data.postValue(UiKitToast(UiKitToast.Value.Generic, "Generic toast"))
                    }, Pair(UiKitToast.Value.Generic.name + " without image") {
                        _data.postValue(UiKitToast(UiKitToast.Value.Generic, "Generic toast without image", showImage = false))
                    }, Pair(UiKitToast.Value.Success.name) {
                        _data.postValue(UiKitToast(UiKitToast.Value.Success, "Success toast"))
                    }, Pair(UiKitToast.Value.Warning.name) {
                        _data.postValue(UiKitToast(UiKitToast.Value.Warning, "Warning toast"))
                    }, Pair(UiKitToast.Value.Error.name) {
                        _data.postValue(UiKitToast(UiKitToast.Value.Error, "Error toast"))
                    }, Pair(UiKitToast.Value.Info.name) {
                        _data.postValue(UiKitToast(UiKitToast.Value.Info, "Info toast"))
                    })).show()
                }
            }
        }

        private fun createPopUpMenu(view: View, options: Array<Pair<String, Action>>): PopupMenu {
            val popUpMenu = PopupMenu(context, view)
            options.forEach {
                popUpMenu.menu.add(it.first)
            }
            popUpMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                options.find { it.first == item?.title }?.second?.invoke()
                return@OnMenuItemClickListener true
            })
            return popUpMenu
        }
    }

    @VisibleForTesting
    data class UiKitCases(val value: Cases) : java.io.Serializable {
        val name = if (value != Cases.StyledDialog) value.name else "Styled Dialog"
    }

    @VisibleForTesting
    enum class Cases {
        Buttons,
        Dialog,
        StyledDialog,
        Result,
        Receipt,
        Outro,
        Input,
        Toast;

        companion object {
            fun toUiKitCases() = listOf(
                UiKitCases(Buttons), UiKitCases(Dialog), UiKitCases(StyledDialog),
                UiKitCases(Result), UiKitCases(Receipt), UiKitCases(Outro), UiKitCases(Input), UiKitCases(Toast)
            )
        }
    }

    companion object {
        const val uiKitRecognition = "fromUiKitShowCase"
    }
}

private typealias Action = () -> Unit