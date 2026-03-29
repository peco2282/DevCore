package com.peco2282.devcore.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
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

  @OptIn(ExperimentalNbtComponent::class)
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

  @OptIn(ExperimentalNbtComponent::class)
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

  @OptIn(ExperimentalNbtComponent::class)
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

  @Test
  fun `gradient text`() {
    val actual = component {
      text("Gradient") {
        gradient(NamedTextColor.RED, NamedTextColor.BLUE)
      }
    }

    // 文字列 "Gradient" は 8 文字
    // 補間されるため、各文字が異なる色を持つはず
    assertEquals(8, (actual as TextComponent).children().size)
    val firstChild = actual.children()[0] as TextComponent
    val lastChild = actual.children()[7] as TextComponent
    
    assertEquals(NamedTextColor.RED, firstChild.color())
    assertEquals(NamedTextColor.BLUE, lastChild.color())
  }

  @Test
  fun `rainbow text`() {
    val actual = component {
      text("Rainbow") {
        rainbow()
      }
    }
    assertEquals(7, (actual as TextComponent).children().size)
  }

  @Test
  fun `weighted gradient text`() {
    val actual = component {
      text("Weighted") {
        gradient {
          colors(NamedTextColor.RED, NamedTextColor.GREEN, NamedTextColor.BLUE)
          weights(1.0, 10.0, 1.0)
        }
      }
    }
    assertEquals(8, (actual as TextComponent).children().size)
    
    // 真ん中あたり (index 4) は GREEN に近いはず (1.0 + 10.0 + 1.0 = 12.0 total weight)
    // index 4 / 7 = 0.57... 0.57 * 12.0 = 6.85...
    // 6.85 は 1.0 (RED) の後、GREEN (10.0) の範囲内。
    val middleChild = actual.children()[4] as TextComponent
    val color = middleChild.color()!!
    // GREEN は #00FF00 (0, 255, 0)
    // 完全な一致は難しいので、緑成分が一番大きいことを確認
    assert(color.green() > color.red() && color.green() > color.blue())
  }
}
