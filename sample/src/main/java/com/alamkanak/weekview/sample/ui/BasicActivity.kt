package com.alamkanak.weekview.sample.ui

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.alamkanak.weekview.WeekViewEntity
import com.alamkanak.weekview.jsr310.WeekViewPagingAdapterJsr310
import com.alamkanak.weekview.jsr310.setDateFormatter
import com.alamkanak.weekview.sample.R
import com.alamkanak.weekview.sample.data.model.CalendarEntity
import com.alamkanak.weekview.sample.data.model.toWeekViewEntity
import com.alamkanak.weekview.sample.databinding.ActivityBasicBinding
import com.alamkanak.weekview.sample.util.*
import com.alamkanak.weekview.sample.util.GenericAction.ShowSnackbar
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val binding: ActivityBasicBinding by lazy {
        ActivityBasicBinding.inflate(layoutInflater)
    }

    private val viewModel by genericViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarContainer.toolbar.setupWithWeekView(binding.weekView)

        val adapter = BasicActivityWeekViewAdapter(
            dragHandler = viewModel::handleDrag,
            loadMoreHandler = viewModel::fetchEvents,
        )
        binding.weekView.adapter = adapter

        binding.weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.viewState.observe(this) { viewState ->
            adapter.submitList(viewState.entities.map {
                when (it) {
                    /* Add a drawable to the Events */
                    is CalendarEntity.Event -> it.copy(
                        icon = AppCompatResources.getDrawable(this, R.drawable.onexp_logo_light)?.toBitmap()?.toCircledBitmap()
                    )
                    is CalendarEntity.BlockedTimeSlot -> it
                }
            })
        }

        viewModel.actions.subscribeToEvents(this) { action ->
            when (action) {
                is ShowSnackbar -> {
                    Snackbar
                        .make(binding.weekView, action.message, Snackbar.LENGTH_SHORT)
                        .setAction("Undo") { action.undoAction() }
                        .show()
                }
            }
        }
    }
}

private class BasicActivityWeekViewAdapter(
    private val dragHandler: (Long, LocalDateTime, LocalDateTime) -> Unit,
    private val loadMoreHandler: (List<YearMonth>) -> Unit
) : WeekViewPagingAdapterJsr310<CalendarEntity>() {

    override fun onCreateEntity(item: CalendarEntity): WeekViewEntity = item.toWeekViewEntity()

    override fun onEventClick(data: CalendarEntity, bounds: RectF) {
        if (data is CalendarEntity.Event) {
            context.showToast("Clicked ${data.title}")
        }
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onDragAndDropFinished(data: CalendarEntity, newStartTime: LocalDateTime, newEndTime: LocalDateTime) {
        if (data is CalendarEntity.Event) {
            dragHandler(data.id, newStartTime, newEndTime)
        }
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${defaultDateTimeFormatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(yearMonthsBetween(startDate, endDate))
    }

    override fun onVerticalScrollPositionChanged(currentOffset: Float, distance: Float) {
        Log.d("BasicActivity", "Scrolling vertically (distance: ${distance.toInt()}, current offset ${currentOffset.toInt()})")
    }

    override fun onVerticalScrollFinished(currentOffset: Float) {
        Log.d("BasicActivity", "Vertical scroll finished (current offset ${currentOffset.toInt()})")
    }
}
