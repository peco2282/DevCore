package com.peco2282.devcore.adventure

import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AdventureExtensionsTest {
  private val plain = PlainTextComponentSerializer.plainText()

  @Test
  fun `progress bar uses requested length and preserves colors`() {
    val bar = progressBar(0.4, length = 10)
    assertEquals("████░░░░░░", plain.serialize(bar))
    assertEquals(NamedTextColor.GREEN, bar.children()[0].color())
    assertEquals(NamedTextColor.DARK_GRAY, bar.children()[1].color())
    assertThrows(IllegalArgumentException::class.java) { progressBar(1.1, 10) }
  }

  @Test
  fun `duration formats short long and clock`() {
    val duration = 3723.seconds
    assertEquals("1h 2m 3s", formatDurationString(duration))
    assertEquals("1 hour, 2 minutes, 3 seconds", formatDurationString(duration, DurationStyle.LONG))
    assertEquals("1:02:03", formatDurationString(duration, DurationStyle.CLOCK))
  }

  @Test
  fun `number formats are locale aware`() {
    assertEquals("1,234,567", formatNumber(1_234_567, Locale.US))
    assertEquals("1.2M", formatCompact(1_234_567, Locale.US))
  }

  @Test
  fun `layout centers wraps and aligns table`() {
    val centered = center(Component.text("A"), width = 30)
    assertEquals("  A", plain.serialize(centered))
    assertEquals(listOf("abc", "def"), wrap(Component.text("abcdef"), 18).map(plain::serialize))
    val table = table { row(Component.text("A"), Component.text("1")); row(Component.text("Long"), Component.text("2")) }
    assertEquals("A     1\nLong  2", plain.serialize(table))
  }

  @Test
  fun `wrap preserves nested component styles`() {
    val source = Component.text().append(Component.text("ab", NamedTextColor.RED))
      .append(Component.text("cd", NamedTextColor.BLUE, TextDecoration.BOLD)).build()
    val lines = wrap(source, 14)
    assertEquals(listOf("ab", "cd"), lines.map(plain::serialize))
    assertEquals(NamedTextColor.RED, lines[0].children()[0].color())
    assertEquals(NamedTextColor.BLUE, lines[1].children()[0].color())
    assertEquals(TextDecoration.State.TRUE, lines[1].children()[0].decoration(TextDecoration.BOLD))
  }

  @Test
  fun `pagination clamps pages and transforms items`() {
    val pages = paginate((1..5), perPage = 2) { Component.text("item$it") }
    assertEquals(3, pages.size)
    assertEquals(1, pages[0].number)
    assertEquals(listOf("item5"), pages[99].items.map(plain::serialize))
  }

  @Test
  fun `animations are deterministic`() {
    val typing = TextAnimation.typewriter(Component.text("abc"))
    assertEquals(listOf("a", "ab", "abc"), (0..2).map { plain.serialize(typing.frame(it)) })
    val blink = TextAnimation.blink(Component.text("!"), on = 1, off = 1)
    assertEquals("!", plain.serialize(blink.frame(0)))
    assertEquals("", plain.serialize(blink.frame(1)))
    assertEquals(0.5, progressAnimation(0.0, 1.0, 3)(1))
  }

  @Test
  fun `book always has a page`() {
    val value = book { title("Rules"); author("Admin") }
    assertEquals(Component.text("Rules"), value.title())
    assertEquals(1, value.pages().size)
  }
}
