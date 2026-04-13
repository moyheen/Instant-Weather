package com.mayokunadeniyi.instantweather.utils

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Created by Mayokun Adeniyi on 7/24/21.
 */

fun String.formatDate(): String {
    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val formatter = SimpleDateFormat("d MMM y, h:mma", Locale.getDefault())
    val parsedDate = runCatching { parser.parse(this) }.getOrNull()
    return if (parsedDate != null) formatter.format(parsedDate) else this
}
