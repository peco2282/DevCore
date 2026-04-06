package com.peco2282.adventure.builder

import com.peco2282.adventure.StyleDsl
import com.peco2282.adventure.component
import com.peco2282.adventure.obfuscated
import com.peco2282.adventure.style
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration


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

  override fun openFile(file: String): Styler =
    apply { builder = builder.clickEvent(ClickEvent.openFile(file)) }

  override fun clickEvent(event: ClickEvent): Styler =
    apply { builder = builder.clickEvent(event) }

  override fun hoverEvent(event: HoverEvent<*>): Styler =
    apply { builder = builder.hoverEvent(event) }

  override fun insertion(text: String): Styler =
    apply { builder = builder.insertion(text) }

  override fun whenTrue(
    condition: Boolean,
    overrider: Styler.() -> Unit
  ): Styler =
    apply { if (condition) mergeStyle(style(overrider)) }

  override fun showText(consumer: Componenter.() -> Unit): Styler =
    apply { builder = builder.hoverEvent(HoverEvent.showText(component(consumer))) }

  override fun noObfuscated(): Styler =
    apply { builder = builder.decoration(TextDecoration.OBFUSCATED, false) }

  override fun noBold(): Styler =
    apply{
    builder = builder.decoration(TextDecoration.BOLD, false)
  }

  override fun noItalic(): Styler =
    apply { builder = builder.decoration(TextDecoration.ITALIC, false)
  }

  override fun noStrikethrough(): Styler =
    apply { builder = builder.decoration(TextDecoration.STRIKETHROUGH, false) }

  override fun noUnderline(): Styler =
    apply { builder = builder.decoration(TextDecoration.UNDERLINED, false) }

  override fun reset(): Styler =
    apply { builder = Style.style() }

  fun build(): Style = builder.build()
}
