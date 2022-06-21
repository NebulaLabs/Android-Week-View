package com.alamkanak.weekview

import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.text.StaticLayout

internal class TextFitter(
    private val viewState: ViewState
) {

    private val spannableStringBuilder = SpannableStringBuilder()

    fun fitAllDayEvent(eventChip: EventChip): Pair<StaticLayout, Bitmap?> {
        val textPaint = viewState.getTextPaint(eventChip.event)
        return Pair(eventChip.getText(includeSubtitle = false).toTextLayout(textPaint, width = Int.MAX_VALUE), eventChip.event.icon)
    }

    fun fitSingleEvent(eventChip: EventChip, availableWidth: Int, availableHeight: Int): Pair<StaticLayout, Bitmap?> {
        return eventChip.fitText(availableWidth, availableHeight)
    }

    private fun EventChip.fitText(availableWidth: Int, availableHeight: Int): Pair<StaticLayout, Bitmap?> {
        val textPaint = viewState.getTextPaint(event)

        var text = getText(includeSubtitle = true)
        val icon = event.icon
        var textLayout = text.toTextLayout(textPaint, width = availableWidth)

        val fitsCompletely = textLayout.height <= availableHeight
        if (fitsCompletely) {
            return Pair(textLayout, icon)
        }

        while (textLayout.height > availableHeight && textLayout.lineCount > 1) {
            // Remove the last lines until there's only a single line left. If it doesn't fit
            // by that point, we need to reduce the text size.
            val startOfLastLine = textLayout.getLineStart(textLayout.lineCount)
            text = text.subSequence(startIndex = 0, endIndex = startOfLastLine - 1).trim()
            textLayout = text.toTextLayout(textPaint, width = availableWidth)
        }

        while (textLayout.height > availableHeight && viewState.adaptiveEventTextSize) {
            // Even a single line doesn't fit. We need to reduce the text size.
            textPaint.textSize -= 1
            textLayout = text.toTextLayout(textPaint, width = Int.MAX_VALUE)
        }

        return Pair(textLayout, icon)
    }

    private fun EventChip.getText(includeSubtitle: Boolean): CharSequence {
        val subtitle = event.subtitle?.takeIf { event.isNotAllDay && includeSubtitle }
        return combineTitleAndSubtitle(
            title = event.title,
            subtitle = subtitle,
            isMultiLine = event.isNotAllDay
        )
    }

    private fun combineTitleAndSubtitle(
        title: CharSequence,
        subtitle: CharSequence?,
        isMultiLine: Boolean
    ): CharSequence = when (subtitle) {
        null -> title
        else -> {
            val separator = if (isMultiLine) "\n" else " "
            spannableStringBuilder.clear()
            spannableStringBuilder
                .append(title)
                .append(separator)
                .append(subtitle)
                .build()
        }
    }
}
