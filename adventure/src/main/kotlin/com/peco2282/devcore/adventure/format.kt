package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.time.Duration
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

enum class DurationStyle { SHORT, LONG, CLOCK }

data class DurationUnits(
  val day: Pair<String, String> = "day" to "days",
  val hour: Pair<String, String> = "hour" to "hours",
  val minute: Pair<String, String> = "minute" to "minutes",
  val second: Pair<String, String> = "second" to "seconds",
  val millisecond: Pair<String, String> = "millisecond" to "milliseconds",
)

fun progressBar(
  progress: Double,
  length: Int,
  filled: String = "█",
  empty: String = "░",
  filledColor: TextColor = NamedTextColor.GREEN,
  emptyColor: TextColor = NamedTextColor.DARK_GRAY,
): Component {
  require(progress.isFinite() && progress in 0.0..1.0) { "Progress must be between 0.0 and 1.0" }
  require(length >= 0) { "Length must be non-negative" }
  val filledCount = floor(progress * length).toInt()
  return Component.text()
    .append(Component.text(filled.repeat(filledCount), filledColor))
    .append(Component.text(empty.repeat(length - filledCount), emptyColor))
    .build()
}

fun formatDurationString(
  duration: Duration,
  style: DurationStyle = DurationStyle.SHORT,
  units: DurationUnits = DurationUnits(),
): String {
  require(!duration.isNegative()) { "Duration must be non-negative" }
  val millis = duration.inWholeMilliseconds
  val days = millis / 86_400_000
  val hours = millis / 3_600_000 % 24
  val minutes = millis / 60_000 % 60
  val seconds = millis / 1_000 % 60
  val milliseconds = millis % 1_000
  if (style == DurationStyle.CLOCK) {
    val hoursTotal = millis / 3_600_000
    return if (hoursTotal > 0) "%d:%02d:%02d".format(Locale.ROOT, hoursTotal, minutes, seconds)
    else "%d:%02d".format(Locale.ROOT, minutes, seconds)
  }
  val values = listOf(days, hours, minutes, seconds, milliseconds)
  if (style == DurationStyle.SHORT) {
    val labels = listOf("d", "h", "m", "s", "ms")
    return values.zip(labels).filter { it.first != 0L }.joinToString(" ") { "${it.first}${it.second}" }.ifEmpty { "0s" }
  }
  val labels = listOf(units.day, units.hour, units.minute, units.second, units.millisecond)
  return values.zip(labels).filter { it.first != 0L }.joinToString(", ") { (value, names) ->
    "$value ${if (value == 1L) names.first else names.second}"
  }.ifEmpty { "0 ${units.second.second}" }
}

fun formatDuration(duration: Duration, style: DurationStyle = DurationStyle.SHORT, units: DurationUnits = DurationUnits()): Component =
  Component.text(formatDurationString(duration, style, units))

fun formatNumber(number: Number, locale: Locale = Locale.getDefault()): String =
  NumberFormat.getNumberInstance(locale).format(number)

fun formatCompact(number: Number, locale: Locale = Locale.getDefault(), fractionDigits: Int = 1): String {
  require(fractionDigits >= 0) { "Fraction digits must be non-negative" }
  val value = number.toDouble()
  val magnitude = abs(value)
  val (divisor, suffix) = when {
    magnitude >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
    magnitude >= 1_000_000_000 -> 1_000_000_000.0 to "B"
    magnitude >= 1_000_000 -> 1_000_000.0 to "M"
    magnitude >= 1_000 -> 1_000.0 to "K"
    else -> return formatNumber(number, locale)
  }
  val formatter = NumberFormat.getNumberInstance(locale).apply {
    maximumFractionDigits = fractionDigits
    minimumFractionDigits = 0
  }
  return formatter.format(value / divisor) + suffix
}

fun ComponentBuilder.progress(progress: Double, length: Int, filled: String = "█", empty: String = "░"): ComponentBuilder =
  append(progressBar(progress, length, filled, empty))

fun ComponentBuilder.duration(duration: Duration, style: DurationStyle = DurationStyle.SHORT, units: DurationUnits = DurationUnits()): ComponentBuilder =
  append(formatDuration(duration, style, units))
