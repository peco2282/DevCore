package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.ComponentDsl
import com.peco2282.devcore.adventure.withStyle
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.ApiStatus

/**
 * A builder interface for creating and manipulating Adventure text components.
 * Provides fluent API methods for appending content, styling, and collecting components.
 */
@ComponentDsl
interface Componenter {
  /**
   * Appends a string content to this componenter.
   *
   * @param content the string content to append
   * @return this componenter for chaining
   */
  infix fun append(content: String): Componenter

  /**
   * Appends a component to this componenter.
   *
   * @param component the component to append
   * @return this componenter for chaining
   */
  infix fun append(component: Component): Componenter

  /**
   * Creates a new componenter scope with the given consumer.
   *
   * @param consumer the consumer lambda that operates on a componenter
   * @return this componenter for chaining
   */
  infix fun create(consumer: Componenter.() -> Unit): Componenter

  /**
   * Joins all appended components into a single component without a separator.
   *
   * @return the joined component
   */
  fun join(): Component

  /**
   * Joins all appended components into a single component with a string separator.
   *
   * @param sep the string separator
   * @return the joined component
   */
  fun join(sep: String): Component

  /**
   * Joins all appended components into a single component with a component separator.
   *
   * @param sep the component separator
   * @return the joined component
   */
  fun join(sep: Component): Component

  /**
   * Joins all appended components into a single component using a join configuration.
   *
   * @param conf the join configuration
   * @return the joined component
   */
  fun join(conf: JoinConfiguration): Component

  /**
   * Collects all appended components into a single component without a separator.
   *
   * @return the collected component
   */
  fun collect(): Component

  /**
   * Collects all appended components into a single component with a string separator.
   *
   * @param separator the string separator
   * @return the collected component
   */
  fun collect(separator: String): Component

  /**
   * Collects all appended components into a single component with a component separator.
   *
   * @param separator the component separator
   * @return the collected component
   */
  fun collect(separator: Component): Component


  /**
   * Conditionally applies a consumer lambda to this componenter if the condition is true.
   *
   * @param condition the boolean condition to evaluate
   * @param consumer the consumer lambda that operates on a componenter, executed only if condition is true
   * @return this componenter for chaining
   */
  fun whenTrue(condition: Boolean, consumer: Componenter.() -> Unit): Componenter

  /**
   * Conditionally applies a consumer lambda to this componenter if the condition is false.
   *
   * @param condition the boolean condition to evaluate
   * @param consumer the consumer lambda that operates on a componenter, executed only if condition is false
   * @return this componenter for chaining
   */
  fun whenFalse(condition: Boolean, consumer: Componenter.() -> Unit): Componenter = whenTrue(!condition, consumer)

  /**
   * Appends a text component with the specified string content.
   *
   * @param content the string content to append
   * @return this componenter for chaining
   */
  fun text(content: String): Componenter = append(content)

  /**
   * Appends a text component with the specified string content and applies additional configuration.
   * This method combines appending text with a nested componenter scope, allowing you to build
   * complex component hierarchies in a fluent way.
   *
   * @param content the string content to append
   * @param consumer the consumer lambda that operates on a componenter to configure the appended text
   * @return this componenter for chaining
   */
  fun text(content: String, consumer: Styler.() -> Unit): Componenter = append(content).withStyle(consumer)

  /**
   * Appends a translatable component with the specified translation key.
   *
   * @param key the translation key
   * @return this componenter for chaining
   */
  infix fun translatable(key: String): Componenter = append(Component.translatable(key))

  /**
   * Appends a translatable component with the specified translation key and component arguments.
   *
   * @param key the translation key
   * @param args the component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, vararg args: Component): Componenter

  /**
   * Appends a translatable component with the specified translation key and a list of component arguments.
   *
   * @param key the translation key
   * @param args the list of component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, args: List<Component>): Componenter

  /**
   * Appends a translatable component with the specified translation key and arguments built using a consumer lambda.
   *
   * @param key the translation key
   * @param consumer the consumer lambda that builds component arguments
   * @return this componenter for chaining
   */
  fun translatable(key: String, consumer: Componenter.() -> Unit): Componenter

  /**
   * Appends a translatable component with the specified translation key and style.
   *
   * @param key the translation key
   * @param style the style to apply to the translatable component
   * @return this componenter for chaining
   */
  fun translatable(key: String, style: Style): Componenter

  /**
   * Appends a translatable component with the specified translation key, style, and component arguments.
   *
   * @param key the translation key
   * @param style the style to apply to the translatable component
   * @param args the component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, style: Style, vararg args: Component): Componenter

  /**
   * Appends a translatable component with the specified translation key, style, and list of arguments.
   *
   * @param key the translation key
   * @param styler the style builder to apply to the translatable component
   * @param args the list of component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, styler: Styler.() -> Unit, args: List<Component>): Componenter

  /**
   * Appends a styled translatable component with arguments built using a nested componenter scope.
   *
   * @param key the translation key
   * @param styler the style builder to apply to the translatable component
   * @param consumer the consumer lambda that builds component arguments
   * @return this componenter for chaining
   */
  fun translatable(key: String, styler: Styler.() -> Unit, consumer: Componenter.() -> Unit): Componenter

  /**
   * Appends a keybind component with the specified keybind key.
   *
   * @param key the keybind key
   * @return this componenter for chaining
   */
  infix fun keybind(key: String): Componenter

  /**
   * Appends a score component with the specified player name and objective.
   *
   * @param name the player name whose score to display
   * @param objective the scoreboard objective name
   * @return this componenter for chaining
   */
  fun score(name: String, objective: String): Componenter


  /**
   * Appends a block NBT component with the specified NBT path and builder configuration.
   *
   * @param nbt the NBT path to query from the block
   * @param consumer the consumer lambda that configures the BlockNBTComponent.Builder
   * @return this componenter for chaining
   */
  @ApiStatus.Experimental
  fun blockNbt(@Language("NBTPath") nbt: String, consumer: BlockNBTComponent.Builder.() -> Unit): Componenter

  /**
   * Appends an entity NBT component with the specified NBT path and builder configuration.
   *
   * @param nbt the NBT path to query from the entity
   * @param consumer the consumer lambda that configures the EntityNBTComponent.Builder
   * @return this componenter for chaining
   */
  @ApiStatus.Experimental
  fun entityNbt(@Language("NBTPath") nbt: String, consumer: EntityNBTComponent.Builder.() -> Unit): Componenter

  /**
   * Operator function to append a string to this componenter using the unary + operator.
   *
   * @return this componenter for chaining
   */
  operator fun String.unaryPlus(): Componenter = this@Componenter.append(this)

  /**
   * Operator function to append a component to this componenter using the unary + operator.
   *
   * @return this componenter for chaining
   */
  operator fun Component.unaryPlus(): Componenter = this@Componenter.append(this)

  /**
   * Applies a style to the last appended component in this componenter.
   *
   * @param style the style to apply to the last component
   * @return this componenter for chaining
   */
  fun styleLast(style: Style): Componenter

  /**
   * Appends a selector component with the specified selector pattern.
   *
   * @param key the selector pattern (e.g., "@a", "@p", "@e[type=cow]")
   * @return this componenter for chaining
   */
  infix fun selector(key: String): Componenter

  /**
   * Sets the insertion text for the last appended component.
   * This text will be inserted into the chat input when the component is shift-clicked.
   *
   * @param text the text to insert when shift-clicked
   * @return this componenter for chaining
   */
  infix fun insertion(text: String): Componenter

  /**
   * Applies a hover event to the last appended component.
   *
   * @param event the hover event to apply
   * @return this componenter for chaining
   */
  infix fun hoverEvent(event: HoverEvent<*>): Componenter

  /**
   * Appends a newline component to this componenter.
   *
   * @return this componenter for chaining
   */
  fun newline(): Componenter


  /**
   * Appends a space component to this componenter.
   *
   * @return this componenter for chaining
   */
  fun space(): Componenter

  /**
   * Iterates over the given iterable and applies an action to each element.
   * The action can use componenter methods to append components based on each element.
   *
   * @param T the type of elements in the iterable
   * @param iterable the iterable to iterate over
   * @param action the action lambda to apply to each element
   * @return this componenter for chaining
   */
  fun <T> forEach(iterable: Iterable<T>, action: Componenter.(T) -> Unit): Componenter
}
