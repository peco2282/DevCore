package com.peco2282.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComponentDslTest {

  @Test
  fun `simple text component`() {
    val actual = component {
      text("Hello")
      space()
      text("World")
    }

    val expected = Component.text()
      .append(Component.text("Hello"))
      .append(Component.space())
      .append(Component.text("World"))
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `styled text`() {
    val actual = component {
      text("Error: ") {
        red()
        bold()
      }
      text("File not found") {
        gray()
      }
    }

    val expected = Component.text()
      .append(Component.text("Error: ", Style.style(NamedTextColor.RED, TextDecoration.BOLD)))
      .append(Component.text("File not found", NamedTextColor.GRAY))
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `nested component with events`() {
    val actual = component {
      text("Click ")
      text("here") {
        blue()
        underline()
        clickEvent(ClickEvent.openUrl("https://example.com"))

        showText {
          text("Go to website")
        }
      }
    }

    val expected = Component.text()
      .append(Component.text("Click "))
      .append(
        Component.text("here")
          .color(NamedTextColor.BLUE)
          .decorate(TextDecoration.UNDERLINED)
          .clickEvent(ClickEvent.openUrl("https://example.com"))
          .hoverEvent(HoverEvent.showText(Component.text("Go to website")))
      )
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `translatable with args`() {
    val actual = component {
      translatable("welcome.user") {
        text("Peco") { yellow() }
        text("Admin") { red() }
      }
    }

    val expected =
      Component.translatable(
        "welcome.user",
        listOf(
          Component.text("Peco", NamedTextColor.YELLOW),
          Component.text("Admin", NamedTextColor.RED)
        )
      )

    assertEquals(expected, actual)
  }

  @Test
  fun `list iteration`() {
    val items = listOf("Apple", "Banana", "Orange")

    val actual = component {
      text("Fruits: ")
      forEach(items) { item ->
        text(item)
        if (item != items.last()) {
          text(", ")
        }
      }
    }

    val expected = Component.text()
      .append(Component.text("Fruits: "))
      .append(Component.text("Apple"))
      .append(Component.text(", "))
      .append(Component.text("Banana"))
      .append(Component.text(", "))
      .append(Component.text("Orange"))
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `block nbt component with absolute position`() {
    val actual = component {
      text("Block Data: ")
      blockNbt("Items[0].id") {
        absoluteWorldPos(10, 64, -10)
        interpret(true)
      }
    }

    val expected = Component.text()
      .append(Component.text("Block Data: "))
      .append(
        Component.blockNBT { builder ->
          builder.nbtPath("Items[0].id")
          builder.absoluteWorldPos(10, 64, -10)
          builder.interpret(true)
        }
      )
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `block nbt component with local position`() {
    val actual = component {
      text("Looking at: ")
      blockNbt("id") {
        localPos(0.0, 1.5, 5.0)
      }
    }

    val expected = Component.text()
      .append(Component.text("Looking at: "))
      .append(
        Component.blockNBT { builder ->
          builder.nbtPath("id")
          builder.localPos(0.0, 1.5, 5.0)
        }
      )
      .build()

    assertEquals(expected, actual)
  }

  @Test
  fun `entity nbt component`() {
    val actual = component {
      text("Player Health: ")
      entityNbt("Health") {
        selector("@p")
      }
    }

    val expected = Component.text()
      .append(Component.text("Player Health: "))
      .append(
        Component.entityNBT { builder ->
          builder.nbtPath("Health")
          builder.selector("@p")
        }
      )
      .build()

    assertEquals(expected, actual)
  }
}
