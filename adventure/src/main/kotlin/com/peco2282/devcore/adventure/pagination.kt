package com.peco2282.devcore.adventure

import java.time.Duration
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

class Paginated<T> internal constructor(private val pages: List<List<T>>) {
  val size: Int get() = pages.size
  operator fun get(page: Int): Page<T> {
    val index = (page - 1).coerceIn(0, pages.lastIndex)
    return Page(index + 1, pages.size, pages[index])
  }
}

data class Page<T>(val number: Int, val total: Int, val items: List<T>)

fun Page<Component>.render(header: (Int, Int) -> Component = { current, total -> Component.text("Page $current/$total") }): Component {
  val result = Component.text().append(header(number, total)).append(Component.newline())
  items.forEachIndexed { index, item ->
    result.append(item)
    if (index < items.lastIndex) result.append(Component.newline())
  }
  return result.build()
}

fun <T, R> paginate(items: Iterable<T>, perPage: Int, transform: (T) -> R): Paginated<R> {
  require(perPage > 0) { "Items per page must be positive" }
  val pages = items.map(transform).chunked(perPage).ifEmpty { listOf(emptyList()) }
  return Paginated(pages)
}

fun <T> Audience.sendPage(
  items: Iterable<T>,
  page: Int = 1,
  perPage: Int = 8,
  command: (Int) -> String,
  render: (T) -> Component,
) {
  val pages = paginate(items, perPage, render)
  val current = pages[page]
  val previous = navButton("«", current.number > 1, current.number - 1, command)
  val next = navButton("»", current.number < current.total, current.number + 1, command)
  sendMessage(Component.text().append(current.render()).append(Component.newline()).append(previous)
    .append(Component.text(" ${current.number}/${current.total} ")).append(next).build())
}

private fun navButton(label: String, enabled: Boolean, page: Int, command: (Int) -> String): Component =
  Component.text(label, if (enabled) NamedTextColor.YELLOW else NamedTextColor.DARK_GRAY)
    .clickEvent(if (enabled) ClickEvent.runCommand(command(page)) else null)

fun <T> Audience.sendPage(items: Iterable<T>, page: Int = 1, perPage: Int = 8, render: (T) -> Component) {
  val snapshot = items.toList()
  fun show(audience: Audience, requested: Int) {
    val pages = paginate(snapshot, perPage, render)
    val current = pages[requested]
    fun callback(label: String, enabled: Boolean, target: Int): Component =
      Component.text(label, if (enabled) NamedTextColor.YELLOW else NamedTextColor.DARK_GRAY).clickEvent(
        if (!enabled) null else ClickEvent.callback(ClickCallback { clicked -> show(clicked, target) },
          ClickCallback.Options.builder().uses(Int.MAX_VALUE).lifetime(Duration.ofMinutes(5)).build())
      )
    audience.sendMessage(Component.text().append(current.render()).append(Component.newline())
      .append(callback("«", current.number > 1, current.number - 1))
      .append(Component.text(" ${current.number}/${current.total} "))
      .append(callback("»", current.number < current.total, current.number + 1)).build())
  }
  show(this, page)
}
