@file:Suppress("UNUSED")

package it.pagopa.swc_smartpos.sharedutils.span

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.core.text.italic

class SpanWithCustomTag(private val kindOfStyleSpan: KindOfStyleSpan, private val tags: Array<String>) {
    enum class KindOfStyleSpan {
        BOLD,
        ITALIC,
        BOLD_ITALIC
    }

    fun stringSpanned(string: String) = string.createBackData()?.spanRespectKindOfStyle(kindOfStyleSpan) ?: string

    private class BackData(val array: Array<Pair<Int, Int>>?, val stringFormatted: String) {
        private inline fun SpannableStringBuilder.boldItalic(
            builderAction: SpannableStringBuilder.() -> Unit
        ): SpannableStringBuilder = inSpans(StyleSpan(Typeface.BOLD_ITALIC), builderAction = builderAction)

        fun spanRespectKindOfStyle(kindOfStyleSpan: KindOfStyleSpan): CharSequence {
            val spannableStringBuilder = SpannableStringBuilder()
            var appoy = 0
            var stringApp = this.stringFormatted
            this.array?.forEach {
                if (it.first - appoy > 0)
                    spannableStringBuilder.append(stringApp.substring(0, it.first - appoy))
                when (kindOfStyleSpan) {
                    KindOfStyleSpan.BOLD -> spannableStringBuilder.bold {
                        append(stringApp.substring(it.first - appoy, it.second - appoy))
                    }
                    KindOfStyleSpan.ITALIC -> spannableStringBuilder.italic {
                        append(stringApp.substring(it.first - appoy, it.second - appoy))
                    }
                    KindOfStyleSpan.BOLD_ITALIC -> spannableStringBuilder.boldItalic {
                        append(stringApp.substring(it.first - appoy, it.second - appoy))
                    }
                }
                stringApp = this.stringFormatted.substring(it.second, this.stringFormatted.length)
                appoy = it.second
            }
            return spannableStringBuilder
        }
    }

    private fun String?.createBackData(): BackData? {
        if (this.isNullOrEmpty()) return null
        val arrayList = ArrayList<Pair<Int, Int>>()
        val arrayFirst = ArrayList<Int>()
        val arraySecond = ArrayList<Int>()
        var lastString: String = this
        while (lastString.changeCustomTags() != null) {
            val pair = lastString.changeCustomTags()
            lastString = pair!!.first
            arrayFirst.add(pair.second[0])
            arraySecond.add(pair.second[1])
        }
        arrayFirst.forEachIndexed { index, it ->
            arrayList.add(Pair(it, arraySecond[index]))
        }
        return BackData(arrayList.toTypedArray(), lastString)
    }

    private fun String?.changeCustomTags(): Pair<String, Array<Int>>? {
        if (this.isNullOrEmpty() || tags.size != 2) return null
        val list = ArrayList<Int>()
        val pair = this.changeOneTag(tags[0])
        if (pair != null) {
            list.add(pair.second)
            val secondPair = pair.first.changeOneTag(tags[1])
            return if (secondPair != null) {
                list.add(secondPair.second)
                Pair(secondPair.first, list.toTypedArray())
            } else
                null
        }
        return null
    }

    private fun String?.changeOneTag(tag: String): Pair<String, Int>? {
        if (this == null) return null
        if (this.length < tag.length) return null
        val charArray = this.toCharArray()
        for (i in 0 until charArray.size + 1 - tag.length) {
            var valueInside = ""
            for (j in tag.indices) {
                valueInside += charArray[i + j]
            }
            if (valueInside == tag) {
                return Pair(this.replaceFirst(tag, ""), i)
            }
        }
        return null
    }

    companion object {
        const val boldTag = "<b>"
        const val endBoldTag = "</b>"
    }
}