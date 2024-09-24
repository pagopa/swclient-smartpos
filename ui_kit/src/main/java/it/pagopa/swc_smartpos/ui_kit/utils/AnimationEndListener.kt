package it.pagopa.swc_smartpos.ui_kit.utils

import android.view.animation.Animation

fun interface AnimationEndListener : Animation.AnimationListener {
    fun onEnd(animation: Animation?)

    //implementation not needed
    override fun onAnimationStart(animation: Animation?) {}

    //implementation not needed
    override fun onAnimationRepeat(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {
        onEnd(animation)
    }
}