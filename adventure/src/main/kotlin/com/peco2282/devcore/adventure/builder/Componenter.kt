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
interface Componenter {
  /**
   * Appends a MiniMessage string content to this componenter with optional tag resolvers.
   *
   * @param content the MiniMessage string content
   * @param tags the tag resolvers for placeholders
   * @return this componenter for chaining
   */
  fun mini(content: String, vararg tags: TagResolver): Componenter

  /**
   * Appends a MiniMessage string content to this componenter with tag resolvers built using a consumer.
   *
   * @param content the MiniMessage string content
   * @param tags the tag resolver builder consumer
   * @return this componenter for chaining
   */
  fun mini(content: String, tags: TagResolverBuilder.() -> Unit): Componenter =
    mini(content, *TagResolverBuilderImpl().apply(tags).build())

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
   * Appends a translatable component with component arguments.
   * The arguments will be substituted into the translation pattern.
   *
   * @param key the translation key
   * @param args the component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, vararg args: Component): Componenter

  /**
   * Appends a translatable component with mixed type arguments.
   * Accepts any type of arguments (String, Number, Component, etc.) which will be converted appropriately.
   *
   * @param key the translation key
   * @param args the translation arguments of any type
   * @return this componenter for chaining
   */
  fun translatableAny(key: String, vararg args: Any): Componenter

  /**
   * Appends a translatable component with the specified translation key and a list of component arguments.
   *
   * @param key the translation key
   * @param args the list of component arguments for the translation
   * @return this componenter for chaining
   */
  fun translatable(key: String, args: List<Component>): Componenter

  /**
   * Appends a translatable component with arguments built using a consumer lambda.
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
   * Appends a selector component with the specified selector pattern and applies additional configuration.
   *
   * @param key the selector pattern
   * @param styler the styler to apply
   * @return this componenter for chaining
   */
  fun selector(key: String, styler: Styler.() -> Unit): Componenter = selector(key).withStyle(styler)

  /**
   * Appends a keybind component with the specified keybind key.
   * Displays the key binding configured by the player (e.g., "key.inventory", "key.jump").
   *
   * @param key the keybind key
   * @return this componenter for chaining
   */
  infix fun keybind(key: String): Componenter

  /**
   * Appends a keybind component with the specified keybind key and applies additional configuration.
   *
   * @param key the keybind key
   * @param styler the styler to apply
   * @return this componenter for chaining
   */
  fun keybind(key: String, styler: Styler.() -> Unit): Componenter = keybind(key).withStyle(styler)

  /**
   * Appends a score component with the specified player name and objective.
   *
   * @param name the player name whose score to display
   * @param objective the scoreboard objective name
   * @return this componenter for chaining
   */
  fun score(name: String, objective: String): Componenter


  /**
   * Appends a score component with the specified player name and objective and applies additional configuration.
   *
   * @param name the player name whose score to display
   * @param objective the scoreboard objective name
   * @param styler the styler to apply
   * @return this componenter for chaining
   */
  fun score(name: String, objective: String, styler: Styler.() -> Unit): Componenter =
    score(name, objective).withStyle(styler)

  /**
   * Appends a block NBT component with the specified NBT path and builder configuration.
   * Queries and displays NBT data from a block at a specific position.
   *
   * @param nbt the NBT path to query from the block
   * @param consumer the consumer lambda that configures the BlockNBTComponent.Builder (including block position)
   * @return this componenter for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun blockNbt(@Language("NBTPath") nbt: String, consumer: BlockNBTComponent.Builder.() -> Unit): Componenter

  /**
   * Appends an entity NBT component with the specified NBT path and builder configuration.
   * Queries and displays NBT data from entities matching a selector.
   *
   * @param nbt the NBT path to query from the entity
   * @param consumer the consumer lambda that configures the EntityNBTComponent.Builder (including entity selector)
   * @return this componenter for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun entityNbt(@Language("NBTPath") nbt: String, consumer: EntityNBTComponent.Builder.() -> Unit): Componenter

  /**
   * Appends a storage NBT component that queries data from command storage.
   * Displays NBT data from persistent command storage at the specified key.
   *
   * @param nbt the NBT path to query from storage
   * @param storage the storage key identifying the storage location
   * @param consumer the consumer lambda that configures the StorageNBTComponent.Builder
   * @return this componenter for chaining
   */
  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: Key,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): Componenter

  /**
   * Appends a storage NBT component that queries data from command storage.
   * Displays NBT data from persistent command storage at the specified key (parsed from string).
   *
   * @param nbt the NBT path to query from storage
   * @param storage the storage key as string (e.g., "minecraft:custom_data")
   * @param consumer the consumer lambda that configures the StorageNBTComponent.Builder
   * @return this componenter for chaining
   */
  fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: String,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): Componenter

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
   * Applies a style to the most recently appended component in this componenter.
   * This allows retroactive styling of the last added component.
   *
   * @param style the style to apply to the last component
   * @return this componenter for chaining
   */
  fun styleLast(style: Style): Componenter

  /**
   * Appends a selector component with the specified selector pattern.
   * Selectors target entities in the game world (e.g., "@a" for all players, "@p" for nearest player, "@e[type=cow]" for cows).
   *
   * @param key the selector pattern (e.g., "@a", "@p", "@e[type=cow]")
   * @return this componenter for chaining
   */
  infix fun selector(key: String): Componenter

  /**
   * Sets the insertion text for the last appended component.
   * When the player shift-clicks this component, the specified text will be inserted into their chat input field.
   *
   * @param text the text to insert when shift-clicked
   * @return this componenter for chaining
   */
  infix fun insertion(text: String): Componenter

  /**
   * Applies a hover event to the last appended component.
   * When the player hovers over this component, the specified hover event will be triggered.
   *
   * @param event the hover event to apply (e.g., show text, show item, show entity)
   * @return this componenter for chaining
   */
  infix fun hoverEvent(event: HoverEvent<*>): Componenter

  /**
   * Appends a newline component to this componenter.
   * Inserts a line break in the text display.
   *
   * @return this componenter for chaining
   */
  fun newline(): Componenter

  /**
   * Appends a space component to this componenter.
   * Inserts a single space character in the text display.
   *
   * @return this componenter for chaining
   */
  fun space(): Componenter

  /**
   * Appends a tab component to this componenter.
   *
   * @return this componenter for chaining
   */
  fun tab(): Componenter = append("\t")

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
   * The action receives both the componenter context and the current element, allowing component building per iteration.
   *
   * @param T the type of elements in the iterable
   * @param iterable the iterable to iterate over
   * @param action the action lambda with componenter receiver to apply to each element
   * @return this componenter for chaining
   */
  fun <T> forEach(iterable: Iterable<T>, action: Componenter.(T) -> Unit): Componenter

  /**
   * Extension property to append this string directly to the componenter.
   * Provides a convenient property-style syntax for appending strings.
   */
  val String.append: Componenter
    get() = append(this)

  /**
   * Extension property to append this string as a translatable component.
   * Uses this string as the translation key for localized text.
   */
  val String.translatable: Componenter
    get() = translatable(this)

  /**
   * Extension property to append this string as a selector component.
   * Uses this string as the entity selector pattern (@a, @p, etc.).
   */
  val String.selector: Componenter
    get() = selector(this)

  /**
   * Extension property to append this string as a keybind component.
   * Uses this string as the keybind key to display the player's configured key.
   */
  val String.keybind: Componenter
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
