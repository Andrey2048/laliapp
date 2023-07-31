package ru.netology.laliapp.utils

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.EditText
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

private val calendar = Calendar.getInstance()

@RequiresApi(Build.VERSION_CODES.O)
fun formatToDate(value: String?): String {
    val transformation = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.ROOT)
        .withZone(ZoneId.systemDefault())

    return transformation.format(Instant.parse(value))
}

@SuppressLint("SimpleDateFormat")
fun epochSecToDate(second: Long): String {
    val date = Date(second * 1_000)
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
    return simpleDateFormat.format(date)
}

@RequiresApi(Build.VERSION_CODES.O)
fun dateToEpochSec(string: String?): Long? {
    return if (string.isNullOrBlank()) null else LocalDate.parse(string)
        .atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond()
}

fun pickDate(editText: EditText?, context: Context?) {
    val datePicker = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        calendar[Calendar.YEAR] = year
        calendar[Calendar.MONTH] = monthOfYear
        calendar[Calendar.DAY_OF_MONTH] = dayOfMonth

        editText?.setText(
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                .format(calendar.time)
        )
    }

    DatePickerDialog(
        context!!,
        datePicker,
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )
        .show()
}

fun pickTime(editText: EditText, context: Context) {
    val timePicker = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute

        editText.setText(
            SimpleDateFormat("HH:mm", Locale.ROOT)
                .format(calendar.time)
        )
    }

    TimePickerDialog(
        context,
        timePicker,
        calendar[Calendar.HOUR_OF_DAY],
        calendar[Calendar.MINUTE],
        true
    )
        .show()
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatToInstant(value: String): String {
    return if (value != " ") {
        val datetime = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        )
            .parse(value)
        val transformation = DateTimeFormatter.ISO_INSTANT
        transformation.format(datetime?.toInstant())
    } else "2023-08-01T17:00:00.000000Z"
}