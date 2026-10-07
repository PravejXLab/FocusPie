package com.pravejxlab.focuspie.ui.live_study

import java.util.Locale

fun Long.toMMSSTime(): String {
    val seconds = this / 1000

    val mins = seconds / 60
    val secs = seconds % 60

    return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
}