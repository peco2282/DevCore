package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.StyleDsl
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.style
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.net.URL


@StyleDsl
@PublishedApi
internal class StyleBuilder @PublishedApi internal constructor() : Styler {
  private var builder: Style.Builder = Style.style()

  infix fun mergeStyle(consumer: Style.Builder.() -> Unit): StyleBuilder =
    apply { builder = builder.apply(consumer) }

  override infix fun mergeStyle(style: Style): StyleBuilder =
    apply { builder = builder.merge(style) }

  override infix fun mergeStyle(style: Component): StyleBuilder =
    apply { builder = builder.merge(style.style()) }

  override infix fun color(color: TextColor): StyleBuilder =
    apply { builder = builder.color(color) }

  override infix fun color(color: Int): StyleBuilder =
    apply { builder = builder.color(TextColor.color(color)) }

  override infix fun color(hex: String): StyleBuilder =
    apply { builder = builder.color(TextColor.fromHexString(hex)) }

  override infix fun colorIfAbsent(color: TextColor): StyleBuilder =
    apply { builder = builder.colorIfAbsent(color) }

  override infix fun colorIfAbsent(color: Int): StyleBuilder =
    apply { builder = builder.colorIfAbsent(TextColor.color(color)) }

  override infix fun colorIfAbsent(hex: String): StyleBuilder =
    apply { builder = builder.colorIfAbsent(TextColor.fromHexString(hex)) }

  override infix fun decoration(decoration: TextDecoration): StyleBuilder =
    apply { builder = builder.decorate(decoration) }

  override infix fun decoration(decoration: Collection<TextDecoration>) =
    apply { builder = builder.decorate(*decoration.toTypedArray()) }

  override infix fun font(font: Key) =
    apply { builder = builder.font(font) }

  override fun clickEvent(component: Component): Styler =
    apply { builder = builder.clickEvent(component.clickEvent()) }

  override fun runCommand(command: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.runCommand(command)) }

  override fun suggestCommand(command: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.suggestCommand(command)) }

  override fun openUrl(url: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.openUrl(url)) }

  override fun openUrl(url: URL): Styler =
    apply { builder = builder.clickEvent(ClickEvent.openUrl(url)) }

  override fun copyToClipboard(text: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.copyToClipboard(text)) }

  override fun openFile(file: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.openFile(file)) }

  override fun changePage(page: Int): Styler =
    apply { builder = builder.clickEvent(ClickEvent.changePage(page)) }

  override fun changePage(page: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.changePage(page)) }

  override fun clickEvent(event: ClickEvent): Styler =
    apply { builder = builder.clickEvent(event) }

  override fun showText(component: Component): Styler =
    apply { builder = builder.hoverEvent(HoverEvent.showText(component)) }

  override fun showText(consumer: ComponentBuilder.() -> Unit): Styler =
    apply { builder = builder.hoverEvent(HoverEvent.showText(component(consumer))) }

  override fun showEntity(key: HoverEvent.ShowEntity): Styler =
    apply { builder = builder.hoverEvent(HoverEvent.showEntity(key)) }

  override fun showItem(key: HoverEvent.ShowItem): Styler =
    apply { builder = builder.hoverEvent(HoverEvent.showItem(key)) }

  override fun hoverEvent(event: HoverEvent<*>): Styler =
    apply { builder = builder.hoverEvent(event) }

  override fun insertion(text: String): Styler =
    apply { builder = builder.insertion(text) }

  override fun whenTrue(
    condition: Boolean,
    overrider: Styler.() -> Unit
  ): Styler =
    apply { if (condition) mergeStyle(style(overrider)) }

  override fun noObfuscated(): Styler =
    apply { builder = builder.decoration(TextDecoration.OBFUSCATED, false) }

  override fun noBold(): Styler =
    apply {
      builder = builder.decoration(TextDecoration.BOLD, false)
    }

  override fun noItalic(): Styler =
    apply {
      builder = builder.decoration(TextDecoration.ITALIC, false)
    }

  override fun noStrikethrough(): Styler =
    apply { builder = builder.decoration(TextDecoration.STRIKETHROUGH, false) }

  override fun noUnderline(): Styler =
    apply { builder = builder.decoration(TextDecoration.UNDERLINED, false) }

  override fun reset(): Styler =
    apply {
      builder = Style.style()
      gradient = null
    }

  private var gradient: GradientImpl? = null

  override fun gradient(vararg colors: TextColor): Styler = apply {
    this.gradient = GradientImpl().apply { colors(*colors) }
  }

  override fun gradient(gradient: Gradient.() -> Unit): Styler = apply {
    this.gradient = GradientImpl().apply(gradient)
  }

  override fun rainbow(): Styler = apply {
    this.gradient = GradientImpl().apply {
      colors(
        TextColor.color(0xFF0000), // Red
        TextColor.color(0xFFAA00), // Gold
        TextColor.color(0xFFFF00), // Yellow
        TextColor.color(0x00FF00), // Green
        TextColor.color(0x00AAAA), // Aqua
        TextColor.color(0x0000FF), // Blue
        TextColor.color(0xAA00AA)  // Light Purple
      )
    }
  }

  override fun getGradient(): Gradient? = gradient

  fun build(): Style = builder.build()
}
