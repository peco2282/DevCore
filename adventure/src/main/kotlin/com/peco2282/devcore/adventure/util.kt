package com.peco2282.devcore.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

internal fun <T> MutableList<T>.updateLast(transformer: (T) -> T) {
  if (isEmpty()) return
  this[lastIndex] = transformer(this[lastIndex])
}

fun String.legacy(char: Char = '§'): Component = if (char == '&') LegacyComponentSerializer.legacyAmpersand().deserialize(this) else if (char == '§')LegacyComponentSerializer.legacySection().deserialize(this) else throw IllegalArgumentException("Invalid char: $char")
fun String.mini(): Component = MiniMessage.miniMessage().deserialize(this)

fun String.component(): Component = Component.text(this)
fun Component.text(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

val String.component get() = Component.text(this)
val Component.text: String get() = LegacyComponentSerializer.legacyAmpersand().serialize(this)
