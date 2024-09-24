package it.pagopa.swc_smartpos.ui_kit.utils

import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowLayoutInfo

fun WindowLayoutInfo?.toFoldableState(): FoldableState {
    if (this?.displayFeatures == null) return FoldableState.NotAFoldable
    return if (this.displayFeatures.isNotEmpty()) {
        when ((this.displayFeatures.getOrNull(0) as? FoldingFeature)?.state) {
            FoldingFeature.State.HALF_OPENED -> FoldableState.Opened
            FoldingFeature.State.FLAT -> FoldableState.Opened
            else -> FoldableState.Normal
        }
    } else
        FoldableState.NotAFoldable
}

enum class FoldableState {
    NotAFoldable,
    Normal,
    Opened
}
