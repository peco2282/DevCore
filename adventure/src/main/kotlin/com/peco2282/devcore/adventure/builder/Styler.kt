package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.StyleDsl
import com.peco2282.devcore.adventure.component
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.net.URL
import java.util.*

/**
 * Interface for styling text components with various text formatting options.
 *
 * Provides methods to apply colors, decorations, fonts, and merge styles to text components.
 */
@StyleDsl
interface Styler {
  /**
   * Merges the given style with the current style.
   *
   * @param style the style to merge
   * @return this Styler instance for chaining
   */
  infix fun mergeStyle(style: Style): Styler

  /**
   * Merges the style from the given component with the current style.
   *
   * @param style the component whose style to merge
   * @return this Styler instance for chaining
   */
  infix fun mergeStyle(style: Component): Styler

  /**
   * Sets the color using an integer RGB value.
   *
   * @param color the color as an integer (e.g., 0xRRGGBB)
   * @return this Styler instance for chaining
   */
  infix fun color(color: Int): Styler

  /**
   * Sets the text color.
   *
   * @param color the text color to apply
   * @return this Styler instance for chaining
   */
  infix fun color(color: TextColor): Styler

  /**
   * Sets the color using a hexadecimal string.
   *
   * @param hex the color as a hexadecimal string (e.g., "#RRGGBB" or "RRGGBB")
   * @return this Styler instance for chaining
   */
  infix fun color(hex: String): Styler

  /**
   * Sets the color using an integer RGB value if no color is currently set.
   *
   * @param color the color as an integer (e.g., 0xRRGGBB)
   * @return this Styler instance for chaining
   */
  infix fun colorIfAbsent(color: Int): Styler

  /**
   * Sets the text color if no color is currently set.
   *
   * @param color the text color to apply
   * @return this Styler instance for chaining
   */
  infix fun colorIfAbsent(color: TextColor): Styler

  /**
   * Sets the color using a hexadecimal string if no color is currently set.
   *
   * @param hex the color as a hexadecimal string (e.g., "#RRGGBB" or "RRGGBB")
   * @return this Styler instance for chaining
   */
  infix fun colorIfAbsent(hex: String): Styler

  /**
   * Applies a text decoration.
   *
   * @param decoration the text decoration to apply (e.g., BOLD, ITALIC)
   * @return this Styler instance for chaining
   */
  infix fun decoration(decoration: TextDecoration): Styler

  /**
   * Applies multiple text decorations.
   *
   * @param decoration the collection of text decorations to apply
   * @return this Styler instance for chaining
   */
  infix fun decoration(decoration: Collection<TextDecoration>): Styler

  /**
   * Sets the font for the text.
   *
   * @param font the font key to apply
   * @return this Styler instance for chaining
   */
  infix fun font(font: Key): Styler

  /**
   * Sets the font for the text using a font key string.
   *
   * @param font the font key as a string
   * @return this Styler instance for chaining
   */
  infix fun font(font: String): Styler = font(Key.key(font))

  /**
   * Applies a click event from another component.
   *
   * @param component the component whose click event to apply
   * @return this Styler instance for chaining
   */
  infix fun clickEvent(component: Component): Styler

  /**
   * Applies a click event that runs a command when clicked.
   *
   * @param command the command to run (e.g., "/help")
   * @return this Styler instance for chaining
   */
  infix fun runCommand(command: String): Styler

  /**
   * Applies a click event that suggests a command in the chat box when clicked.
   *
   * @param command the command to suggest
   * @return this Styler instance for chaining
   */
  infix fun suggestCommand(command: String): Styler

  /**
   * Applies a click event that opens a URL when clicked.
   *
   * @param url the URL as a string to open
   * @return this Styler instance for chaining
   */
  infix fun openUrl(url: String): Styler

  /**
   * Applies a click event that opens a URL when clicked.
   *
   * @param url the URL to open
   * @return this Styler instance for chaining
   */
  infix fun openUrl(url: URL): Styler

  /**
   * Applies a click event that copies text to the clipboard.
   *
   * @param text the text to copy
   * @return this Styler instance for chaining
   */
  infix fun copyToClipboard(text: String): Styler

  /**
   * Applies a click event that opens a file when clicked.
   *
   * @param file the file path to open
   * @return this Styler instance for chaining
   */
  infix fun openFile(file: String): Styler

  /**
   * Applies a click event that changes the page of a book.
   *
   * @param page the page number to change to
   * @return this Styler instance for chaining
   */
  fun changePage(page: Int): Styler = clickEvent(ClickEvent.changePage(page))

  /**
   * Applies a click event that changes the page of a book.
   *
   * @param page the page number as a string
   * @return this Styler instance for chaining
   */
  fun changePage(page: String): Styler = clickEvent(ClickEvent.changePage(page))

  /**
   * Applies a click event to the text.
   *
   * @param event the click event to apply
   * @return this Styler instance for chaining
   */
  infix fun clickEvent(event: ClickEvent): Styler

  /**
   * Applies a hover event that shows the given component.
   *
   * @param component the component to show
   * @return this Styler instance for chaining
   */
  infix fun showText(component: Component): Styler

  /**
   * Applies a hover event that shows the given text.
   *
   * @param text the text to show
   * @return this Styler instance for chaining
   */
  infix fun showText(text: String): Styler = showText(text.component)

  /**
   * Applies a hover event that shows a component built with a DSL.
   *
   * @param consumer the function to build the hover text component
   * @return this Styler instance for chaining
   */
  fun showText(consumer: ComponentBuilder.() -> Unit): Styler

  /**
   * Applies a hover event that shows an entity.
   *
   * @param key the show entity hover event to apply
   * @return this Styler instance for chaining
   */
  infix fun showEntity(key: HoverEvent.ShowEntity): Styler

  /**
   * Applies a hover event that shows an entity with the given key, UUID, and name.
   *
   * @param key the entity type key
   * @param uuid the entity UUID
   * @param name the entity name (defaults to empty component)
   * @return this Styler instance for chaining
   */
  fun showEntity(key: Key, uuid: UUID, name: Component = Component.empty()): Styler =
    showEntity(HoverEvent.ShowEntity.showEntity(key, uuid, name))

  /**
   * Applies a hover event that shows an item.
   *
   * @param key the show item hover event to apply
   * @return this Styler instance for chaining
   */
  infix fun showItem(key: HoverEvent.ShowItem): Styler

  /**
   * Applies a hover event that shows an item with the given key and count.
   *
   * @param key the item type key
   * @param count the item count
   * @return this Styler instance for chaining
   */
  fun showItem(key: Key, count: Int): Styler = showItem(HoverEvent.ShowItem.showItem(key, count))

  /**
   * Applies a hover event that shows an item with the given key, count, and data components.
   *
   * @param key the item type key
   * @param count the item count
   * @param data the item data components
   * @return this Styler instance for chaining
   */
  fun showItem(key: Key, count: Int, data: Map<Key, DataComponentValue>): Styler =
    showItem(HoverEvent.ShowItem.showItem(key, count, data))

  /**
   * Applies a hover event to the text.
   *
   * @param event the hover event to apply
   * @return this Styler instance for chaining
   */
  infix fun hoverEvent(event: HoverEvent<*>): Styler

  /**
   * Sets the text to be inserted when shift-clicked.
   *
   * @param text the text to insert
   * @return this Styler instance for chaining
   */
  infix fun insertion(text: String): Styler

  /**
   * Applies styling conditionally when the condition is true.
   *
   * @param condition the condition to evaluate
   * @param overrider the styling function to apply if condition is true
   * @return this Styler instance for chaining
   */
  fun whenTrue(condition: Boolean, overrider: Styler.() -> Unit): Styler

  /**
   * Applies styling conditionally when the condition is false.
   *
   * @param condition the condition to evaluate
   * @param overrider the styling function to apply if condition is false
   * @return this Styler instance for chaining
   */
  fun whenFalse(condition: Boolean, overrider: Styler.() -> Unit): Styler =
    whenTrue(!condition, overrider)

  /**
   * Applies red color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun red(): Styler = color(NamedTextColor.RED)

  /**
   * Applies green color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun green(): Styler = color(NamedTextColor.GREEN)

  /**
   * Applies yellow color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun yellow(): Styler = color(NamedTextColor.YELLOW)

  /**
   * Applies blue color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun blue(): Styler = color(NamedTextColor.BLUE)

  /**
   * Applies dark blue color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun darkBlue(): Styler = color(NamedTextColor.DARK_BLUE)

  /**
   * Applies light purple color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun purple(): Styler = color(NamedTextColor.LIGHT_PURPLE)

  /**
   * Applies dark purple color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun darkPurple(): Styler = color(NamedTextColor.DARK_PURPLE)

  /**
   * Applies aqua color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun aqua(): Styler = color(NamedTextColor.AQUA)

  /**
   * Applies gray color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun gray(): Styler = color(NamedTextColor.GRAY)

  /**
   * Applies dark gray color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun darkGray(): Styler = color(NamedTextColor.DARK_GRAY)

  /**
   * Applies white color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun white(): Styler = color(NamedTextColor.WHITE)

  /**
   * Applies black color to the text.
   *
   * @return this Styler instance for chaining
   */
  fun black(): Styler = color(NamedTextColor.BLACK)

  /**
   * Applies obfuscated text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun obfuscated(): Styler = decoration(TextDecoration.OBFUSCATED)

  /**
   * Applies bold text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun bold(): Styler = decoration(TextDecoration.BOLD)

  /**
   * Applies italic text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun italic(): Styler = decoration(TextDecoration.ITALIC)

  /**
   * Applies strikethrough text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun strikethrough(): Styler = decoration(TextDecoration.STRIKETHROUGH)

  /**
   * Applies underline text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun underline(): Styler = decoration(TextDecoration.UNDERLINED)

  /**
   * Removes obfuscated text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun noObfuscated(): Styler

  /**
   * Removes bold text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun noBold(): Styler

  /**
   * Removes italic text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun noItalic(): Styler

  /**
   * Removes strikethrough text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun noStrikethrough(): Styler

  /**
   * Removes underline text decoration.
   *
   * @return this Styler instance for chaining
   */
  fun noUnderline(): Styler

  /**
   * Resets all styling to default values.
   *
   * @return this Styler instance for chaining
   */
  fun reset(): Styler

  /**
   * Applies a gradient of colors to the text.
   *
   * @param colors the colors to use in the gradient
   * @return this Styler instance for chaining
   */
  fun gradient(vararg colors: TextColor): Styler

  /**
   * Applies a gradient of colors to the text using a DSL builder.
   *
   * @param gradient the gradient builder consumer
   * @return this Styler instance for chaining
   */
  fun gradient(gradient: Gradient.() -> Unit): Styler

  /**
   * Applies a rainbow effect to the text.
   *
   * @return this Styler instance for chaining
   */
  fun rainbow(): Styler

  /**
   * Internal method to retrieve the gradient implementation.
   */
  fun getGradient(): Gradient?
}
