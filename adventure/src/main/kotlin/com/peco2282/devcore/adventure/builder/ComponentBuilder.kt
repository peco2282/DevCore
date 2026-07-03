package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.ComponentDsl
import com.peco2282.devcore.adventure.ExperimentalNbtComponent
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.withStyle
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.*
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.ApiStatus

/**
 * A builder interface for creating and manipulating Adventure text components.
 * Provides fluent API methods for appending content, styling, and collecting components.
 */
@ComponentDsl
interface ComponentBuilder {
  /**
   * Appends an empty component.
   *
   * @return this component builder for chaining
   */
  fun empty(): ComponentBuilder = append(Component.empty())

  /**
   * Appends a MiniMessage string content to this component builder with optional tag resolvers.
   *
   * @param content the MiniMessage string content
   * @param tags the tag resolvers for placeholders
   * @return this component builder for chaining
   */
  fun mini(content: String, vararg tags: TagResolver): ComponentBuilder

  /**
   * Appends a MiniMessage string content to this component builder with tag resolvers built using a consumer.
   *
   * @param content the MiniMessage string content
   * @param tags the tag resolver builder consumer
   * @return this component builder for chaining
   */
  fun mini(content: String, tags: TagResolverBuilder.() -> Unit): ComponentBuilder =
    mini(content, *TagResolverBuilderImpl().apply(tags).build())

  /**
   * Appends a string content to this component builder.
   *
   * @param content the string content to append
   * @return this component builder for chaining
   */
  infix fun append(content: String): ComponentBuilder

  /**
   * Appends a component to this component builder.
   *
   * @param component the component to append
   * @return this component builder for chaining
   */
  infix fun append(component: Component): ComponentBuilder

  /**
   * Creates a new component builder scope with the given consumer.
   *
   * @param consumer the consumer lambda that operates on a component builder
   * @return this component builder for chaining
   */
  infix fun create(consumer: ComponentBuilder.() -> Unit): ComponentBuilder

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
   * Joins all appended components into a single component using a join configuration DSL.
   *
   * @param consumer the join configuration builder consumer
   * @return the joined component
   */
  fun join(consumer: JoinConfiguration.Builder.() -> Unit): Component =
    join(JoinConfiguration.builder().apply(consumer).build())

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
   * Conditionally applies a consumer lambda to this component builder if the condition is true.
   *
   * @param condition the boolean condition to evaluate
   * @param consumer the consumer lambda that operates on a component builder, executed only if condition is true
   * @return this component builder for chaining
   */
  fun whenTrue(condition: Boolean, consumer: ComponentBuilder.() -> Unit): ComponentBuilder

  /**
   * Conditionally applies a consumer lambda to this component builder if the condition is false.
   *
   * @param condition the boolean condition to evaluate
   * @param consumer the consumer lambda that operates on a component builder, executed only if condition is false
   * @return this component builder for chaining
   */
  fun whenFalse(condition: Boolean, consumer: ComponentBuilder.() -> Unit): ComponentBuilder = whenTrue(!condition, consumer)

  /**
   * Conditionally applies one of two consumer lambdas to this component builder based on a predicate result.
   * Evaluates the predicate function and applies the ifTrue consumer if the result is true,
   * or the ifFalse consumer if the result is false.
   *
   * @param predicate the function that returns a boolean to determine which consumer to apply
   * @param ifTrue the consumer lambda that operates on a component builder, executed when predicate returns true
   * @param ifFalse the consumer lambda that operates on a component builder, executed when predicate returns false
   * @return this component builder for chaining
   */

  fun compute(predicate: () -> Boolean, ifTrue: ComponentBuilder.() -> Unit, ifFalse: ComponentBuilder.() -> Unit): ComponentBuilder = if (predicate()) apply(ifTrue) else apply(ifFalse)

  /**
   * Appends a text component with the specified string content.
   *
   * @param content the string content to append
   * @return this component builder for chaining
   */
  fun text(content: String): ComponentBuilder = append(content)

  /**
   * Appends a text component with the specified string content and applies additional configuration.
   * This method combines appending text with a nested component builder scope, allowing you to build
   * complex component hierarchies in a fluent way.
   *
   * @param content the string content to append
   * @param consumer the consumer lambda that operates on a component builder to configure the appended text
   * @return this component builder for chaining
   */
  fun text(content: String, consumer: Styler.() -> Unit): ComponentBuilder = append(content).withStyle(consumer)

  /**
   * Appends a translatable component with the specified translation key.
   *
   * @param key the translation key
   * @return this component builder for chaining
   */
  infix fun translatable(key: String): ComponentBuilder = append(Component.translatable(key))

  /**
   * Appends a translatable component with component arguments.
   * The arguments will be substituted into the translation pattern.
   *
   * @param key the translation key
   * @param args the component arguments for the translation
   * @return this component builder for chaining
   */
  fun translatable(key: String, vararg args: Component): ComponentBuilder

  /**
   * Appends a translatable component with mixed type arguments.
   * Accepts any type of arguments (String, Number, Component, etc.) which will be converted appropriately.
   *
   * @param key the translation key
   * @param args the translation arguments of any type
   * @return this component builder for chaining
   */
  fun translatableAny(key: String, vararg args: Any): ComponentBuilder

  /**
   * Appends a translatable component with the specified translation key and a list of component arguments.
   *
   * @param key the translation key
   * @param args the list of component arguments for the translation
   * @return this component builder for chaining
   */
  fun translatable(key: String, args: List<Component>): ComponentBuilder

  /**
   * Appends a translatable component with arguments built using a consumer lambda.
   *
   * @param key the translation key
   * @param consumer the consumer lambda that builds component arguments
   * @return this component builder for chaining
   */
  fun translatable(key: String, consumer: ComponentBuilder.() -> Unit): ComponentBuilder

  /**
   * Appends a translatable component with the specified translation key and style.
   *
   * @param key the translation key
   * @param style the style to apply to the translatable component
   * @return this component builder for chaining
   */
  fun translatable(key: String, style: Style): ComponentBuilder

  /**
   * Appends a translatable component with the specified translation key, style, and component arguments.
   *
   * @param key the translation key
   * @param style the style to apply to the translatable component
   * @param args the component arguments for the translation
   * @return this component builder for chaining
   */
  fun translatable(key: String, style: Style, vararg args: Component): ComponentBuilder

  /**
   * Appends a translatable component with the specified translation key, style, and list of arguments.
   *
   * @param key the translation key
   * @param styler the style builder to apply to the translatable component
   * @param args the list of component arguments for the translation
   * @return this component builder for chaining
   */
  fun translatable(key: String, styler: Styler.() -> Unit, args: List<Component>): ComponentBuilder

  /**
   * Appends a styled translatable component with arguments built using a nested component builder scope.
   *
   * @param key the translation key
   * @param styler the style builder to apply to the translatable component
   * @param consumer the consumer lambda that builds component arguments
   * @return this component builder for chaining
   */
  fun translatable(key: String, styler: Styler.() -> Unit, consumer: ComponentBuilder.() -> Unit): ComponentBuilder

  /**
   * Appends a selector component with the specified selector pattern and applies additional configuration.
   *
   * @param key the selector pattern
   * @param styler the styler to apply
   * @return this component builder for chaining
   */
  fun selector(key: String, styler: Styler.() -> Unit): ComponentBuilder = selector(key).withStyle(styler)

  /**
   * Appends a keybind component with the specified keybind key.
   * Displays the key binding configured by the player (e.g., "key.inventory", "key.jump").
   *
   * @param key the keybind key
   * @return this component builder for chaining
   */
  infix fun keybind(key: String): ComponentBuilder

  /**
   * Appends a keybind component with the specified keybind key and applies additional configuration.
   *
   * @param key the keybind key
   * @param styler the styler to apply
   * @return this component builder for chaining
   */
  fun keybind(key: String, styler: Styler.() -> Unit): ComponentBuilder = keybind(key).withStyle(styler)

  /**
   * Appends a score component with the specified player name and objective.
   *
   * @param name the player name whose score to display
   * @param objective the scoreboard objective name
   * @return this component builder for chaining
   */
  fun score(name: String, objective: String): ComponentBuilder


  /**
   * Appends a score component with the specified player name and objective and applies additional configuration.
   *
   * @param name the player name whose score to display
   * @param objective the scoreboard objective name
   * @param styler the styler to apply
   * @return this component builder for chaining
   */
  fun score(name: String, objective: String, styler: Styler.() -> Unit): ComponentBuilder =
    score(name, objective).withStyle(styler)

  /**
   * Appends a block NBT component with the specified NBT path and builder configuration.
   * Queries and displays NBT data from a block at a specific position.
   *
   * @param nbt the NBT path to query from the block
   * @param consumer the consumer lambda that configures the BlockNBTComponent.Builder (including block position)
   * @return this component builder for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun blockNbt(@Language("NBTPath") nbt: String, consumer: BlockNBTComponent.Builder.() -> Unit): ComponentBuilder

  /**
   * Appends an entity NBT component with the specified NBT path and builder configuration.
   * Queries and displays NBT data from entities matching a selector.
   *
   * @param nbt the NBT path to query from the entity
   * @param consumer the consumer lambda that configures the EntityNBTComponent.Builder (including entity selector)
   * @return this component builder for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun entityNbt(@Language("NBTPath") nbt: String, consumer: EntityNBTComponent.Builder.() -> Unit): ComponentBuilder

  /**
   * Appends a storage NBT component that queries data from command storage.
   * Displays NBT data from persistent command storage at the specified key.
   *
   * @param nbt the NBT path to query from storage
   * @param storage the storage key identifying the storage location
   * @param consumer the consumer lambda that configures the StorageNBTComponent.Builder
   * @return this component builder for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: Key,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): ComponentBuilder

  /**
   * Appends a storage NBT component that queries data from command storage.
   * Displays NBT data from persistent command storage at the specified key (parsed from string).
   *
   * @param nbt the NBT path to query from storage
   * @param storage the storage key as string (e.g., "minecraft:custom_data")
   * @param consumer the consumer lambda that configures the StorageNBTComponent.Builder
   * @return this component builder for chaining
   */
  fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: String,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): ComponentBuilder

  /**
   * Operator function to append a string to this component builder using the unary + operator.
   *
   * @return this component builder for chaining
   */
  operator fun String.unaryPlus(): ComponentBuilder = this@ComponentBuilder.append(this)

  /**
   * Operator function to append a component to this component builder using the unary + operator.
   *
   * @return this component builder for chaining
   */
  operator fun Component.unaryPlus(): ComponentBuilder = this@ComponentBuilder.append(this)

  /**
   * Operator function that returns this component builder.
   * This is useful for syntax like `+"string" { style }`.
   *
   * @return this component builder for chaining
   */
  operator fun unaryPlus(): ComponentBuilder = this

  /**
   * Retroactively applies a styling consumer to the most recently appended component.
   *
   * @param style the style to apply to the last component
   * @return this component builder for chaining
   */
  fun styleLast(style: Style): ComponentBuilder

  /**
   * Appends a selector component with the specified selector pattern.
   * Selectors target entities in the game world (e.g., "@a" for all players, "@p" for nearest player, "@e[type=cow]" for cows).
   *
   * @param key the selector pattern (e.g., "@a", "@p", "@e[type=cow]")
   * @return this component builder for chaining
   */
  infix fun selector(key: String): ComponentBuilder

  /**
   * Sets the insertion text for the last appended component.
   * When the player shift-clicks this component, the specified text will be inserted into their chat input field.
   *
   * @param text the text to insert when shift-clicked
   * @return this component builder for chaining
   */
  infix fun insertion(text: String): ComponentBuilder

  /**
   * Applies a hover event to the last appended component.
   * When the player hovers over this component, the specified hover event will be triggered.
   *
   * @param event the hover event to apply (e.g., show text, show item, show entity)
   * @return this component builder for chaining
   */
  infix fun hoverEvent(event: HoverEvent<*>): ComponentBuilder

  /**
   * Appends a newline component to this component builder.
   * Inserts a line break in the text display.
   *
   * @return this component builder for chaining
   */
  fun newline(): ComponentBuilder

  /**
   * Appends a space component to this component builder.
   * Inserts a single space character in the text display.
   *
   * @return this component builder for chaining
   */
  fun space(): ComponentBuilder

  /**
   * Appends specified number of space components.
   *
   * @param count the number of spaces to append
   * @return this component builder for chaining
   */
  fun space(count: Int): ComponentBuilder = apply { repeat(count) { space() } }

  /**
   * Repeats the given action specified number of times.
   *
   * @param count the number of times to repeat
   * @param consumer the action to repeat
   * @return this component builder for chaining
   */
  fun repeat(count: Int, consumer: ComponentBuilder.(Int) -> Unit): ComponentBuilder = apply {
    for (i in 0 until count) {
      consumer(i)
    }
  }

  /**
   * Appends a tab component to this component builder.
   *
   * @return this component builder for chaining
   */
  fun tab(): ComponentBuilder = append("\t")

  /**
   * Creates a new component join configuration using a DSL builder.
   *
   * @param consumer the join configuration builder consumer
   * @return the built join configuration
   */
  fun joinConfiguration(consumer: JoinConfiguration.Builder.() -> Unit): JoinConfiguration =
    JoinConfiguration.builder().apply(consumer).build()

  /**
   * Iterates over the given iterable and applies an action to each element.
   * The action receives both the component builder context and the current element, allowing component building per iteration.
   *
   * @param T the type of elements in the iterable
   * @param iterable the iterable to iterate over
   * @param action the action lambda with component builder receiver to apply to each element
   * @return this component builder for chaining
   */
  fun <T> forEach(iterable: Iterable<T>, action: ComponentBuilder.(T) -> Unit): ComponentBuilder
  fun <T> forEach(array: Array<T>, action: ComponentBuilder.(T) -> Unit): ComponentBuilder = forEach(array.toList(), action)

  /**
   * Appends a text component that shows the given text when hovered.
   *
   * @param text the text content to append
   * @param hover the text to show when the player hovers over this component
   * @return this component builder for chaining
   */
  fun textWithHover(text: String, hover: String): ComponentBuilder = text(text) { showText(hover) }

  /**
   * Appends a text component that shows the given component when hovered.
   *
   * @param text the text content to append
   * @param hover the component to show when the player hovers over this component
   * @return this component builder for chaining
   */
  fun textWithHover(text: String, hover: Component): ComponentBuilder = text(text) { showText(hover) }

  /**
   * Appends a text component that opens the given URL when clicked.
   *
   * @param text the text content to append
   * @param url the URL to open when clicked
   * @return this component builder for chaining
   */
  fun textWithUrl(text: String, url: String): ComponentBuilder = text(text) { openUrl(url) }

  /**
   * Appends a text component that runs the given command when clicked.
   *
   * @param text the text content to append
   * @param command the command to run when clicked
   * @return this component builder for chaining
   */
  fun textWithCommand(text: String, command: String): ComponentBuilder = text(text) { runCommand(command) }

  /**
   * Appends a text component that suggests the given command when clicked.
   *
   * @param text the text content to append
   * @param command the command to suggest when clicked
   * @return this component builder for chaining
   */
  fun textWithSuggestion(text: String, command: String): ComponentBuilder = text(text) { suggestCommand(command) }

  /**
   * Appends a text component that copies the given text to the clipboard when clicked.
   *
   * @param text the text content to append
   * @param copy the text to copy to the clipboard
   * @return this component builder for chaining
   */
  fun textWithCopy(text: String, copy: String): ComponentBuilder = text(text) { copyToClipboard(copy) }

  /**
   * Applies the given styler to the last appended component.
   */
  operator fun (Styler.() -> Unit).invoke(): ComponentBuilder = withStyle(this)

  /**
   * Applies the given styler to the last appended component.
   * This operator allows for syntax like `+"string" { color(RED) }`.
   *
   * @param consumer the styling consumer
   * @return this component builder for chaining
   */
  operator fun String.invoke(consumer: Styler.() -> Unit): ComponentBuilder = this@ComponentBuilder.append(this).withStyle(consumer)

  /**
   * Applies the given styler to the last appended component.
   * This operator allows for syntax like `+"string" { color(RED) }`.
   *
   * @param consumer the styling consumer
   * @return this component builder for chaining
   */
  operator fun Component.invoke(consumer: Styler.() -> Unit): ComponentBuilder = this@ComponentBuilder.append(this).withStyle(consumer)

  /**
   * Applies the given styler to the last appended component.
   * This operator allows for syntax like `+"string" { color(RED) }`.
   *
   * @param consumer the styling consumer
   * @return this component builder for chaining
   */
  fun withStyle(consumer: Styler.() -> Unit): ComponentBuilder

//  /**
//   * Applies the given styler to the last appended component.
//   * This operator allows for syntax like `+"string" { color(RED) }`.
//   *
//   * @param consumer the styling consumer
//   * @return this component builder for chaining
//   */
//  operator fun invoke(consumer: Styler.() -> Unit): ComponentBuilder = withStyle(consumer)
  /// Moved to [com.peco2282.devcore.adventure.infixer.kt]

  /**
   * Applies styling if the given value is not null.
   *
   * @param T the type of value
   * @param value the value to check
   * @param consumer the action to apply if value is not null
   * @return this component builder for chaining
   */
  fun <T : Any> ifPresent(value: T?, consumer: ComponentBuilder.(T) -> Unit): ComponentBuilder = apply {
    if (value != null) {
      consumer(value)
    }
  }

  /**
   * Extension property to append this string directly to the component builder.
   * Provides a convenient property-style syntax for appending strings.
   */
  val String.append: ComponentBuilder
    get() = append(this)

  /**
   * Extension property to append this string as a translatable component.
   * Uses this string as the translation key for localized text.
   */
  val String.translatable: ComponentBuilder
    get() = translatable(this)

  /**
   * Extension property to append this string as a selector component.
   * Uses this string as the entity selector pattern (@a, @p, etc.).
   */
  val String.selector: ComponentBuilder
    get() = selector(this)

  /**
   * Extension property to append this string as a keybind component.
   * Uses this string as the keybind key to display the player's configured key.
   */
  val String.keybind: ComponentBuilder
    get() = keybind(this)

  /**
   * Extension property to convert this string into a Component using auto-detection.
   * Automatically detects the component format type (legacy section, legacy ampersand, MiniMessage, or plain text)
   * and deserializes the string accordingly.
   *
   * @see component
   */
  val String.convert: Component
    get() = component
}
