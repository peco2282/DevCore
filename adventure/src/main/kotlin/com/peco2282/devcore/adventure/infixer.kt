package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.Componenter
import com.peco2282.devcore.adventure.builder.Styler
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BuildableComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Appends a text component with the given content to this component.
 *
 * @param content The string content to append
 * @return The component with the appended content
 */
infix fun Component.append(content: String) = append(Component.text(content))


/**
 * Appends a text component with the given content to this component builder.
 *
 * @param content The string content to append
 * @return The component builder with the appended content
 */
infix fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.append(content: String) =
  append(Component.text(content))

/**
 * Operator function to append a string to this component builder using the + operator.
 *
 * @param content The string content to append
 * @return The component builder with the appended content
 */
operator fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.plus(content: String) =
  append(content)

/**
 * Operator function to append a component to this component builder using the + operator.
 *
 * @param component The component to append
 * @return The component builder with the appended component
 */
operator fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.plus(component: Component) =
  append(component)

/**
 * Operator function to append a string to this component builder using the += operator.
 *
 * @param content The string content to append
 */
operator fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.plusAssign(content: String) {
  append(content)
}

/**
 * Operator function to append a component to this component builder using the += operator.
 *
 * @param component The component to append
 */
operator fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.plusAssign(component: Component) {
  append(component)
}

/**
 * Applies a style to this component builder using a builder consumer.
 *
 * @param consumer The style builder consumer
 * @return The component builder with the applied style
 */
infix fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.withStyle(consumer: Style.Builder.() -> Unit) =
  style(consumer)

/**
 * Applies a style to this component builder.
 *
 * @param style The style to apply
 * @return The component builder with the applied style
 */
infix fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.withStyle(style: Style) =
  style(style)

/**
 * Sets the color of this component builder.
 *
 * @param color The text color to apply
 * @return The component builder with the applied color
 */
infix fun <C : BuildableComponent<C, B>, B : ComponentBuilder<C, B>> ComponentBuilder<C, B>.withColor(color: TextColor) =
  color(color)

/**
 * Sets the color of this style.
 *
 * @param color The text color to apply
 * @return The style with the applied color
 */
infix fun Style.withColor(color: TextColor) =
  color(color)

/**
 * Sets the color of this style using an integer RGB value.
 *
 * @param color The RGB color value
 * @return The style with the applied color
 */
infix fun Style.withColor(color: Int) =
  color(TextColor.color(color))

/**
 * Sets the font of this style.
 *
 * @param font The font key to apply
 * @return The style with the applied font
 */
infix fun Style.withFont(font: Key) =
  font(font)

/**
 * Sets the hover event of this style using a component.
 *
 * @param event The component to show on hover
 * @return The style with the applied hover event
 */
infix fun Style.withHoverEvent(event: Component) =
  hoverEvent(event.asHoverEvent())

/**
 * Sets the insertion text of this style.
 *
 * @param insertion The text to insert when shift-clicked
 * @return The style with the applied insertion
 */
infix fun Style.withInsertion(insertion: String) =
  insertion(insertion)

/**
 * Sets the click event of this style.
 *
 * @param event The click event to apply
 * @return The style with the applied click event
 */
infix fun Style.withClickEvent(event: ClickEvent) =
  clickEvent(event)

/**
 * Sets the click event of this style to open a URL.
 *
 * @param url The URL to open when clicked
 * @return The style with the applied click event
 */
infix fun Style.withOpenURL(url: String) =
  clickEvent(ClickEvent.openUrl(url))

/**
 * Sets the click event of this style to suggest a command.
 *
 * @param command The command to suggest when clicked
 * @return The style with the applied click event
 */
infix fun Style.withSuggestCommand(command: String) =
  clickEvent(ClickEvent.suggestCommand(command))

/**
 * Sets the click event of this style to open a file.
 *
 * @param file The file path to open when clicked
 * @return The style with the applied click event
 */
infix fun Style.withOpenFile(file: String) =
  clickEvent(ClickEvent.openFile(file))

/**
 * Sets the click event of this style to run a command from a string.
 *
 * @param event The command string to run
 * @return The style with the applied click event
 */
infix fun Style.withClickCommand(event: String) =
  clickEvent(ClickEvent.runCommand(event))

/**
 * Adds a text decoration to this style.
 *
 * @param decoration The text decoration to apply
 * @return The style with the applied decoration
 */
infix fun Style.withDecoration(decoration: TextDecoration) =
  decorate(decoration)

/**
 * Adds multiple text decorations to this style.
 *
 * @param decoration The collection of text decorations to apply
 * @return The style with the applied decorations
 */
infix fun Style.withDecoration(decoration: Collection<TextDecoration>) =
  decorate(*decoration.toTypedArray())


/**
 * Applies a style to the last component added to this Componenter using a DSL builder pattern.
 *
 * This infix function provides a convenient way to style the most recently added component
 * in the builder chain. The consumer lambda receives a [Styler] instance that allows
 * configuring style properties.
 *
 * @param consumer A lambda with receiver that configures the style builder
 * @return This [Componenter] instance for method chaining
 *
 * Example usage:
 * ```kotlin
 * component {
 *     text("Hello")
 *     text("World") withStyle {
 *         color(NamedTextColor.RED)
 *         decoration(TextDecoration.BOLD, true)
 *     }
 * }
 * ```
 */
inline infix fun Componenter.withStyle(crossinline consumer: Styler.() -> Unit): Componenter =
  styleLast(style(consumer))

/**
 * Sets the color of the last component added to this Componenter.
 */
infix fun Componenter.withColor(color: TextColor): Componenter =
  styleLast(Style.style(color))

/**
 * Sets the color of the last component added to this Componenter using an integer RGB value.
 */
infix fun Componenter.withColor(color: Int): Componenter =
  styleLast(Style.style(TextColor.color(color)))

/**
 * Sets the font of the last component added to this Componenter.
 */
infix fun Componenter.withFont(font: Key): Componenter =
  styleLast(Style.style().font(font).build())

/**
 * Sets the font of the last component added to this Componenter using a font key string.
 */
infix fun Componenter.withFont(font: String): Componenter =
  withFont(Key.key(font))

/**
 * Sets the bold decoration of the last component added to this Componenter.
 */
fun Componenter.bold(): Componenter =
  styleLast(Style.style(TextDecoration.BOLD))

/**
 * Sets the italic decoration of the last component added to this Componenter.
 */
fun Componenter.italic(): Componenter =
  styleLast(Style.style(TextDecoration.ITALIC))

/**
 * Sets the underline decoration of the last component added to this Componenter.
 */
fun Componenter.underline(): Componenter =
  styleLast(Style.style(TextDecoration.UNDERLINED))

/**
 * Sets the strikethrough decoration of the last component added to this Componenter.
 */
fun Componenter.strikethrough(): Componenter =
  styleLast(Style.style(TextDecoration.STRIKETHROUGH))

/**
 * Sets the obfuscated decoration of the last component added to this Componenter.
 */
fun Componenter.obfuscated(): Componenter =
  styleLast(Style.style(TextDecoration.OBFUSCATED))
