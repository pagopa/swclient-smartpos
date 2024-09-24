package it.pagopa.swc_smartpos.ui_kit.fragments

import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import it.pagopa.swc_smart_pos.ui_kit.R
import it.pagopa.swc_smart_pos.ui_kit.databinding.IntroBinding
import it.pagopa.swc_smartpos.ui_kit.uiBase.BaseDataBindingFragment
import it.pagopa.swc_smartpos.ui_kit.utils.AnimationEndListener

abstract class BaseIntroFragment<Act : AppCompatActivity> : BaseDataBindingFragment<IntroBinding, Act>() {
    abstract val logoClick: () -> Unit
    abstract val image: Int
    abstract val mainText: Int
    abstract val mainBtnText: Int
    abstract val mainBtnAction: () -> Unit
    abstract val questionText: Int
    abstract val secondaryBtnText: Int
    abstract val secondaryBtnAction: () -> Unit
    override fun viewBinding() = binding(IntroBinding::inflate)
    override fun setupUI() {
        binding.mainIntro.isVisible = false
        binding.llNotWorking.isVisible = false
        binding.ivMain.setImageResource(image)
        binding.mainBtn.text = getTextSafely(mainBtnText)
        binding.tvDescription.text = getTextSafely(mainText)
        binding.questionText.text = getTextSafely(questionText)
        binding.secondaryBtn.setText(getTextSafely(secondaryBtnText))
    }

    override fun setupListeners() {
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.animation_fade_in)
        val fadeIn2 = AnimationUtils.loadAnimation(context, R.anim.animation_fade_in)
        val fadeIn3 = AnimationUtils.loadAnimation(context, R.anim.animation_fade_in)
        fadeIn?.setAnimationListener(AnimationEndListener {
            binding.mainIntro.startAnimation(fadeIn2)
            binding.mainIntro.isVisible = true
        })
        fadeIn2?.setAnimationListener(AnimationEndListener {
            binding.llNotWorking.startAnimation(fadeIn3)
            binding.llNotWorking.isVisible = true
        })
        binding.mainMenu.startAnimation(fadeIn)
        binding.mainBtn.setOnClickListener {
            mainBtnAction.invoke()
        }
        binding.secondaryBtn.setOnClickListener { secondaryBtnAction.invoke() }
        binding.mainMenu.setOnClickListener {
            logoClick.invoke()
        }
    }
}