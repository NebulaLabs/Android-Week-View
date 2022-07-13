package com.alamkanak.weekview.sample.util

import android.content.Context
import android.graphics.*
import android.widget.Toast
import java.time.LocalDate
import java.time.YearMonth

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun yearMonthsBetween(startDate: LocalDate, endDate: LocalDate): List<YearMonth> {
    val yearMonths = mutableListOf<YearMonth>()
    val maxYearMonth = endDate.yearMonth
    var currentYearMonth = startDate.yearMonth

    while (currentYearMonth <= maxYearMonth) {
        yearMonths += currentYearMonth
        currentYearMonth = currentYearMonth.plusMonths(1)
    }

    return yearMonths
}

private val LocalDate.yearMonth: YearMonth
    get() = YearMonth.of(year, month)

/** Copied from here: [Gist](https://gist.github.com/jewelzqiu/c0633c9f3089677ecf85?permalink_comment_id=3587995#gistcomment-3587995) */
fun Bitmap.toCircledBitmap(): Bitmap {
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