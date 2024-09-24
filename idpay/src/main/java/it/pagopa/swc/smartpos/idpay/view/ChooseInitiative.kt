package it.pagopa.swc.smartpos.idpay.view

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import it.pagopa.swc.smartpos.app_shared.header.HeaderView
import it.pagopa.swc.smartpos.idpay.R
import it.pagopa.swc.smartpos.idpay.databinding.ChooseInitiativeBinding
import it.pagopa.swc.smartpos.idpay.databinding.ItemInitiativeBinding
import it.pagopa.swc.smartpos.idpay.model.Initiatives
import it.pagopa.swc.smartpos.idpay.model.SaleModel
import it.pagopa.swc.smartpos.idpay.second_screen.showSecondScreenIntro
import it.pagopa.swc.smartpos.idpay.uiBase.BaseDataBindingFragmentApp
import it.pagopa.swc.smartpos.idpay.view_model.ChooseInitiativeViewModel
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseRecyclerViewStableIds
import it.pagopa.swc_smartpos.ui_kit.utils.disableScroll
import it.pagopa.swc_smartpos.ui_kit.utils.getSerializableExtra
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.Serializable

class ChooseInitiative : BaseDataBindingFragmentApp<ChooseInitiativeBinding>() {
    private val viewModel: ChooseInitiativeViewModel by viewModels()
    override fun viewBinding() = binding(ChooseInitiativeBinding::inflate)
    override val layoutId: Int get() = R.layout.choose_initiative
    override val backPress: () -> Unit = { this.backToIntroFragment() }
    override val header: HeaderView = HeaderView(
        HeaderView.HeaderElement(R.drawable.arrow_back_primary, backPress),
        null,
        HeaderView.HeaderElement(it.pagopa.swc_smart_pos.ui_kit.R.drawable.home_primary, backPress),
        R.color.white
    )

    override fun setupOnCreate() {
        super.setupOnCreate()
        arguments?.getSerializableExtra(initiativesArg, Initiatives::class.java)?.let {
            viewModel.setInitiatives(it)
        }
        setFragmentResultListener(initiativesArgResultKey) { requestKey, bundle ->
            if (requestKey == initiativesArgResultKey) {
                viewModel.setInitiatives(
                    bundle.getSerializableExtra(
                        initiativesArg,
                        Initiatives::class.java
                    )
                )
                clearFragmentResult(initiativesArgResultKey)
            }
        }
    }

    override fun setupObservers() {
        viewModel.viewModelScope.launch {
            viewModel.initiatives.collectLatest {
                binding.llNoInitiative.isVisible = it == null || it.initiatives.isNullOrEmpty()
                binding.mainNsv.isVisible = it != null && it.initiatives?.isNotEmpty() == true
                if (it != null)
                    binding.rvInitiative.adapter = Adapter(it.toItemInitiativeList())
            }
        }
    }

    private fun Initiatives?.toItemInitiativeList(): List<ItemInitiative> {
        return ArrayList<ItemInitiative>().apply {
            this@toItemInitiativeList?.initiatives?.forEach {
                this.add(ItemInitiative(it, R.drawable.logo) { initiative ->
                    mainActivity?.viewModel?.setModel(SaleModel(initiative = initiative))
                    findNavController().navigate(R.id.action_chooseInitiative_to_chooseImportFragment)
                })
            }
        }.toList()
    }

    override fun setupListeners() {
        binding.goToAssistance.setOnClickListener {
            findNavController().navigate(R.id.action_global_WebViewFragment)
        }
    }

    override fun setupUI() {
        super.setupUI()
        mainActivity?.viewModel?.voidModel()
        mainActivity?.showSecondScreenIntro()
        binding.rvInitiative.disableScroll()
    }

    data class ItemInitiative(
        val model: Initiatives.InitiativeModel,
        @DrawableRes val icon: Int,
        val action: (Initiatives.InitiativeModel) -> Unit
    ) : Serializable

    private inner class Adapter(val list: List<ItemInitiative>) :
        BaseRecyclerViewStableIds<ItemInitiative, ItemInitiativeBinding>(list) {
        override fun viewBinding() = binding(ItemInitiativeBinding::inflate)
        override fun bind(
            context: Context,
            item: ItemInitiative,
            pos: Int,
            binding: ItemInitiativeBinding
        ) {
            binding.divider.isVisible = pos != list.size - 1
            binding.logo.setImageResource(item.icon)
            binding.stringDescription.text = item.model.name
            binding.root.setOnClickListener {
                item.action.invoke(item.model)
            }
        }
    }

    companion object {
        const val initiativesArgResultKey = "initiativesArgResultKey"
        const val initiativesArg = "initiativesArg"
    }
}