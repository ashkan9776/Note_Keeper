package com.ahoura.notekeeper.ui.components

import com.ahoura.notekeeper.domain.model.AppLanguage
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * Formats a [LocalDateTime] based on the current app language.
 * If Persian, it returns a Jalali date string. Otherwise, it uses Gregorian.
 */
fun LocalDateTime.formatLocalized(language: AppLanguage): String {
    return if (language == AppLanguage.PERSIAN) {
        val date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
        val persianDate = PersianDate(date)
        val formatter = PersianDateFormat("Y/m/d H:i")
        formatter.format(persianDate).toPersianDigits()
    } else {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH)
        this.format(formatter)
    }
}

fun LocalDateTime.formatShortLocalized(language: AppLanguage): String {
    return if (language == AppLanguage.PERSIAN) {
        val date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
        val persianDate = PersianDate(date)
        val formatter = PersianDateFormat("j F")
        formatter.format(persianDate).toPersianDigits()
    } else {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        this.format(formatter)
    }
}

fun String.toPersianDigits(): String {
    val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return this.map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
}
