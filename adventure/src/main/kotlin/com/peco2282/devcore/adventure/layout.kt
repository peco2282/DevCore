package com.peco2282.devcore.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

object TextWidth {
  private val narrow = setOf('!', ',', '.', ':', ';', 'i', '|', '\'', '`')
  private val wide = setOf('@', 'M', 'W', 'm', 'w')

  fun of(component: Component): Int = width(component, false)

  private fun width(component: Component, inheritedBold: Boolean): Int {
    val bold = component.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE ||
      (component.decoration(TextDecoration.BOLD) == TextDecoration.State.NOT_SET && inheritedBold)
    val own = if (component is TextComponent) component.content().sumOf { charWidth(it) + if (bold && it != ' ') 1 else 0 } else 0
    return own + component.children().sumOf { width(it, bold) }
  }

  fun of(text: String): Int = text.sumOf(::charWidth)

  private fun charWidth(char: Char): Int = when {
    char == ' ' -> 4
    char in narrow -> 2
    char in wide -> 6
    char.code > 0x7f -> 10
    else -> 5
  } + 1
}

fun center(component: Component, width: Int = 320): Component {
  val remaining = (width - TextWidth.of(component)).coerceAtLeast(0)
  val spaces = remaining / TextWidth.of(" ") / 2
  return Component.text(" ".repeat(spaces)).append(component)
}

fun line(width: Int = 320, char: Char = '-', color: TextColor = NamedTextColor.GRAY): Component {
  require(width >= 0) { "Width must be non-negative" }
  val count = width / TextWidth.of(char.toString())
  return Component.text(char.toString().repeat(count), color)
}

fun wrap(component: Component, maxWidth: Int): List<Component> {
  require(maxWidth > 0) { "Maximum width must be positive" }
  data class StyledChar(val value: Char, val style: Style)
  val chars = mutableListOf<StyledChar>()
  fun flatten(node: Component, parentStyle: Style) {
    val effectiveStyle = node.style().merge(parentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)
    if (node is TextComponent) node.content().forEach { chars += StyledChar(it, effectiveStyle) }
    node.children().forEach { flatten(it, effectiveStyle) }
  }
  flatten(component, Style.empty())
  if (chars.isEmpty()) return listOf(Component.empty())
  val result = mutableListOf<Component>()
  var current = Component.text()
  var hasContent = false
  var width = 0
  fun flush() {
    result += current.build()
    current = Component.text()
    hasContent = false
    width = 0
  }
  for ((char, style) in chars) {
    if (char == '\n') {
      flush()
      continue
    }
    val bold = style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE
    val charWidth = TextWidth.of(char.toString()) + if (bold && char != ' ') 1 else 0
    if (hasContent && width + charWidth > maxWidth) {
      flush()
    }
    current.append(Component.text(char.toString()).style(style))
    hasContent = true
    width += charWidth
  }
  if (hasContent) flush()
  return result
}

class TableBuilder internal constructor() {
  internal val rows = mutableListOf<List<Component>>()
  fun row(vararg cells: Component) { rows += cells.toList() }
}

fun table(separator: Component = Component.text("  "), block: TableBuilder.() -> Unit): Component {
  val rows = TableBuilder().apply(block).rows
  val columns = rows.maxOfOrNull { it.size } ?: 0
  val widths = (0 until columns).map { column -> rows.maxOfOrNull { row -> row.getOrNull(column)?.let(TextWidth::of) ?: 0 } ?: 0 }
  val result = Component.text()
  rows.forEachIndexed { rowIndex, row ->
    row.forEachIndexed { column, cell ->
      result.append(cell)
      if (column < row.lastIndex) {
        val spaces = ((widths[column] - TextWidth.of(cell)) / TextWidth.of(" ")).coerceAtLeast(0)
        result.append(Component.text(" ".repeat(spaces))).append(separator)
      }
    }
    if (rowIndex < rows.lastIndex) result.append(Component.newline())
  }
  return result.build()
}
