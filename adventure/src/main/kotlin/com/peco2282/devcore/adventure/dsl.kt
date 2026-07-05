package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilderImpl
import com.peco2282.devcore.adventure.builder.ComponentBuilder
import com.peco2282.devcore.adventure.builder.StyleBuilder
import com.peco2282.devcore.adventure.builder.Styler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.Style

/**
 * Creates a text component using a DSL builder pattern.
 *
 * This function provides a convenient way to construct Adventure text components using
 * a builder DSL. The consumer lambda receives a [ComponentBuilder] instance that allows
 * building the component structure declaratively.
 *
 * @param consumer A lambda with receiver that configures the component builder
 * @return The built [Component] instance
 *
 * Example usage:
 * ```kotlin
 * val message = component {
 *     text("Hello, ")
 *     text("World!") {
 *         color(NamedTextColor.RED)
 *     }
 * }
 * ```
 */
fun component(consumer: ComponentBuilder.() -> Unit): Component =
  ComponentBuilderImpl().apply(consumer).join()


/**
 * Creates a text component using a DSL builder pattern with a custom joiner function.
 *
 * This function provides an advanced way to construct Adventure text components using
 * a builder DSL with a customizable joining strategy. Unlike the simpler [component] overload,
 * this allows you to specify how the built components should be combined.
 *
 * The consumer lambda receives a [ComponentBuilder] instance that allows building the component
 * structure declaratively. The joiner function determines how the components are assembled
 * into the final [Component] result.
 *
 * @param joiner A function that takes a [ComponentBuilder] and returns a [Component]. This controls
 *               how the built components are joined together. Defaults to [ComponentBuilder.join]
 *               which concatenates components without a separator.
 * @param consumer A lambda with receiver that configures the component builder
 * @return The built [Component] instance as determined by the joiner function
 *
 * Example usage with default joiner:
 * ```kotlin
 * val message = component { // Uses default joiner
 *     append("Hello, ")
 *     append("World!")
 * }
 * ```
 *
 * Example usage with custom separator joiner:
 * ```kotlin
 * val list = component(joiner = { it.join(" | ") }) {
 *     append("Item 1")
 *     append("Item 2")
 *     append("Item 3")
 * }
 * // Result: "Item 1 | Item 2 | Item 3"
 * ```
 *
 * Example usage with collect joiner:
 * ```kotlin
 * val collected = component(joiner = ComponentBuilder::collect) {
 *     append("First")
 *     append("Second")
 *     append("Third")
 * }
 * ```
 *
 * Example usage with custom join configuration:
 * ```kotlin
 * val formatted = component(joiner = { 
 *     it.join(JoinConfiguration.separator(", ".convert)
 *         .prefix("[".convert)
 *         .suffix("]".convert))
 * }) {
 *     append("A")
 *     append("B")
 *     append("C")
 * }
 * // Result: "[A, B, C]"
 * ```
 */
fun component(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit): Component =
  ComponentBuilderImpl().apply(consumer).let(joiner)


/**
 * Creates a text component using a DSL builder pattern with a custom join configuration.
 *
 * This function provides an advanced way to construct Adventure text components using
 * a builder DSL with a customizable [JoinConfiguration]. The joiner lambda receives a
 * [JoinConfiguration.Builder] instance that allows you to define separators, prefixes,
 * suffixes, and other joining behaviors for the component parts.
 *
 * @param joiner A lambda with receiver that configures the [JoinConfiguration.Builder].
 *               This controls how the built components are joined together, including
 *               separator, prefix, suffix, and other joining options.
 * @param consumer A lambda with receiver that configures the component builder
 * @return The built [Component] instance joined according to the specified configuration
 *
 * Example usage with separator:
 * ```kotlin
 * val list = component(joiner = {
 *     separator(Component.text(", "))
 * }) {
 *     append("Apple")
 *     append("Banana")
 *     append("Orange")
 * }
 * // Result: "Apple, Banana, Orange"
 * ```
 *
 * Example usage with prefix and suffix:
 * ```kotlin
 * val bracketed = component(joiner = {
 *     separator(Component.text(" | "))
 *     prefix(Component.text("["))
 *     suffix(Component.text("]"))
 * }) {
 *     append("Item 1")
 *     append("Item 2")
 *     append("Item 3")
 * }
 * // Result: "[Item 1 | Item 2 | Item 3]"
 * ```
 *
 * Example usage with styled separator:
 * ```kotlin
 * val styledList = component(joiner = {
 *     separator(Component.text(" → ").color(NamedTextColor.GRAY))
 * }) {
 *     append("Start")
 *     append("Middle")
 *     append("End")
 * }
 * // Result: "Start → Middle → End" (with gray arrows)
 * ```
 */
@JvmName("componentJoinConfiguration")
fun component(joiner: JoinConfiguration.Builder.() -> Unit, consumer: ComponentBuilder.() -> Unit): Component =
  ComponentBuilderImpl().apply(consumer).join(JoinConfiguration.builder().apply(joiner).build())

/**
 * Creates a compact text component using a DSL builder pattern.
 *
 * This function provides a way to construct Adventure text components using a builder DSL
 * and then compact the result. Compacting a component merges adjacent components with
 * similar styling to reduce redundancy and optimize the component structure.
 *
 * @param isCompact Whether to compact the component. Currently this parameter is present
 *                  for API consistency but the component is always compacted when this
 *                  function is called.
 * @param consumer A lambda with receiver that configures the component builder
 * @return The built and compacted [Component] instance
 *
 * Example usage:
 * ```kotlin
 * val message = component(isCompact = true) {
 *     append("Hello, ")
 *     append("World!")
 * }
 * // Adjacent components with compatible styles are merged
 * ```
 *
 * Example comparing normal and compact:
 * ```kotlin
 * // Without compacting - may have multiple adjacent text components
 * val normal = component {
 *     append("Part 1 ")
 *     append("Part 2 ")
 *     append("Part 3")
 * }
 *
 * // With compacting - adjacent compatible components are merged
 * val compact = component(isCompact = true) {
 *     append("Part 1 ")
 *     append("Part 2 ")
 *     append("Part 3")
 * }
 * ```
 */
fun component(isCompact: Boolean, consumer: ComponentBuilder.() -> Unit) =
  ComponentBuilderImpl().apply(consumer).join().let { if (isCompact) it.compact() else it }

/**
 * Creates a compact text component using a DSL builder pattern with a parent style context.
 *
 * This function provides a way to construct Adventure text components using a builder DSL
 * and then compact the result within the context of a parent style. Compacting a component
 * with a parent style merges adjacent components with similar styling while considering the
 * parent's style properties, which helps reduce redundancy and optimize the component structure
 * more effectively than compacting without a parent style.
 *
 * When compacting with a parent style, the component hierarchy is optimized by:
 * - Merging adjacent components that share the same effective style when combined with the parent
 * - Removing redundant style declarations that duplicate the parent's style
 * - Flattening unnecessary nesting while preserving the visual appearance
 *
 * @param isCompact Whether to compact the component. If `true`, the component is compacted
 *                  using the provided parent style. If `false`, the component is returned as-is.
 * @param parentStyle The parent style context to use during compacting. This represents the
 *                    style that would be inherited from a parent component, allowing the compacting
 *                    process to make more informed decisions about which style properties are redundant.
 * @param consumer A lambda with receiver that configures the component builder
 * @return The built [Component] instance, compacted with the parent style if `isCompact` is `true`
 *
 * Example usage with parent style:
 * ```kotlin
 * val parentStyle = style {
 *     color(NamedTextColor.WHITE)
 *     decoration(TextDecoration.BOLD, false)
 * }
 *
 * val message = component(isCompact = true, parentStyle = parentStyle) {
 *     append("This inherits parent style ")
 *     append("and gets compacted accordingly")
 * }
 * // Components are compacted considering the parent's white color and non-bold decoration
 * ```
 *
 * Example comparing compact with and without parent style:
 * ```kotlin
 * val baseStyle = style {
 *     color(NamedTextColor.GRAY)
 * }
 *
 * // Without parent style context - may retain redundant style declarations
 * val withoutContext = component(isCompact = true) {
 *     append("Gray text") { color(NamedTextColor.GRAY) }
 *     append(" more gray") { color(NamedTextColor.GRAY) }
 * }
 *
 * // With parent style context - removes redundant gray color declarations
 * val withContext = component(isCompact = true, parentStyle = baseStyle) {
 *     append("Gray text") { color(NamedTextColor.GRAY) }
 *     append(" more gray") { color(NamedTextColor.GRAY) }
 * }
 * // The second version is more optimized as it knows gray is already the parent color
 * ```
 *
 * Example with nested components:
 * ```kotlin
 * val containerStyle = style {
 *     color(NamedTextColor.YELLOW)
 *     decoration(TextDecoration.ITALIC, true)
 * }
 *
 * val content = component(isCompact = true, parentStyle = containerStyle) {
 *     append("This is yellow and italic by default ")
 *     append("This too!") {
 *         // Explicit yellow color here is redundant and can be optimized away
 *         color(NamedTextColor.YELLOW)
 *     }
 *     append(" But this is different") {
 *         color(NamedTextColor.RED)
 *         decoration(TextDecoration.ITALIC, false)
 *     }
 * }
 * ```
 */
fun component(isCompact: Boolean, parentStyle: Style, consumer: ComponentBuilder.() -> Unit) =
  ComponentBuilderImpl().apply(consumer).join().let { if (isCompact) it.compact(parentStyle) else it }

/**
 * Creates a text style using a DSL builder pattern.
 *
 * This function provides a convenient way to construct Adventure text styles using
 * a builder DSL. The consumer lambda receives a [Styler] instance that allows
 * configuring style properties such as color, decorations, click events, and more.
 *
 * @param consumer A lambda with receiver that configures the style builder
 * @return The built [Style] instance
 *
 * Example usage:
 * ```kotlin
 * val customStyle = style {
 *     color(NamedTextColor.GOLD)
 *     decoration(TextDecoration.BOLD, true)
 *     clickEvent(ClickEvent.runCommand("/help"))
 * }
 * ```
 */
inline fun style(consumer: Styler.() -> Unit): Style =
  StyleBuilder().apply(consumer).build()
