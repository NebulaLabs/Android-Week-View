package com.alamkanak.weekview.sample.data.model

import android.graphics.*
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.text.style.TypefaceSpan
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.setEndTime
import com.alamkanak.weekview.jsr310.setStartTime
import com.alamkanak.weekview.sample.R
import java.time.LocalDateTime

sealed class CalendarEntity {

    data class Event(
        val id: Long,
        val title: CharSequence,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val location: CharSequence,
        val color: Int,
        val isAllDay: Boolean,
        val isCanceled: Boolean,
        /** Ideally, this bitmap must have a low memory footprint */
        val icon: Bitmap?
    ) : CalendarEntity()

    data class BlockedTimeSlot(
        val id: Long,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime
    ) : CalendarEntity()
}

fun CalendarEntity.toWeekViewEntity(): WeekViewEntity {
    return when (this) {
        is CalendarEntity.Event -> toWeekViewEntity()
        is CalendarEntity.BlockedTimeSlot -> toWeekViewEntity()
    }
}

fun CalendarEntity.Event.toWeekViewEntity(): WeekViewEntity {
    val backgroundColor = if (!isCanceled) color else Color.WHITE
    val textColor = if (!isCanceled) Color.WHITE else color
    val borderWidthResId = if (!isCanceled) R.dimen.no_border_width else R.dimen.border_width

    val style = WeekViewEntity.Style.Builder()
        .setTextColor(textColor)
        .setBackgroundColor(backgroundColor)
        .setBorderWidthResource(borderWidthResId)
        .setBorderColor(color)
        .build()

    val title = SpannableStringBuilder(title).apply {
        val titleSpan = TypefaceSpan("sans-serif-medium")
        setSpan(titleSpan, 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, title.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    val subtitle = SpannableStringBuilder(location).apply {
        if (isCanceled) {
            setSpan(StrikethroughSpan(), 0, location.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    return WeekViewEntity.Event.Builder(this)
        .setId(id)
        .setTitle(title)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setSubtitle(subtitle)
        .setAllDay(isAllDay)
        .setStyle(style)
        .setIcon(icon?.getCircledBitmap())
        .build()
}

fun CalendarEntity.BlockedTimeSlot.toWeekViewEntity(): WeekViewEntity {
    val style = WeekViewEntity.Style.Builder()
        .setBackgroundColorResource(R.color.gray_alpha10)
        .setCornerRadius(0)
        .build()

    return WeekViewEntity.BlockedTime.Builder()
        .setId(id)
        .setStartTime(startTime)
        .setEndTime(endTime)
        .setStyle(style)
        .build()
}

/** Copied from here: [Gist](https://gist.github.com/jewelzqiu/c0633c9f3089677ecf85?permalink_comment_id=3587995#gistcomment-3587995) */
private fun Bitmap.getCircledBitmap(): Bitmap {
    val output = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawCircle(this.width / 2f, this.height / 2f, this.width / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}
