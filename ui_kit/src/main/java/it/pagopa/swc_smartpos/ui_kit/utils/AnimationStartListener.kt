package it.pagopa.swc_smartpos.ui_kit.utils

import android.view.animation.Animation

fun interface AnimationStartListener : Animation.AnimationListener {
    fun onStart(animation: Animation?)

    //implementation not needed
    override fun onAnimationEnd(animation: Animation?) {}

    //implementation not needed
    override fun onAnimationRepeat(animation: Animation?) {}
    override fun onAnimationStart(animation: Animation?) {
        onStart(animation)
    }
}