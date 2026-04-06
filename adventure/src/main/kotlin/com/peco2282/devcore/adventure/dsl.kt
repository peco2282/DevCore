package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import com.peco2282.devcore.adventure.builder.Componenter
import com.peco2282.devcore.adventure.builder.StyleBuilder
import com.peco2282.devcore.adventure.builder.Styler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style

/**
 * Creates a text component using a DSL builder pattern.
 *
 * This function provides a convenient way to construct Adventure text components using
 * a builder DSL. The consumer lambda receives a [Componenter] instance that allows
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
fun component(consumer: Componenter.() -> Unit): Component =
  ComponentBuilder().apply(consumer).join()


/**
 * Creates a text component using a DSL builder pattern with a custom joiner function.
 *
 * This function provides an advanced way to construct Adventure text components using
 * a builder DSL with a customizable joining strategy. Unlike the simpler [component] overload,
 * this allows you to specify how the built components should be combined.
 *
 * The consumer lambda receives a [Componenter] instance that allows building the component
 * structure declaratively. The joiner function determines how the components are assembled
 * into the final [Component] result.
 *
 * @param joiner A function that takes a [Componenter] and returns a [Component]. This controls
 *               how the built components are joined together. Defaults to [Componenter.join]
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
 * val collected = component(joiner = Componenter::collect) {
 *     append("First")
 *     append("Second")
 *     append("Third")
 * }
 * ```
 *
 * Example usage with custom join configuration:
 * ```kotlin
 * val formatted = component(joiner = { 
 *     it.join(JoinConfiguration.separator(Component.text(", "))
 *         .prefix(Component.text("["))
 *         .suffix(Component.text("]")))
 * }) {
 *     append("A")
 *     append("B")
 *     append("C")
 * }
 * // Result: "[A, B, C]"
 * ```
 */
fun component(joiner: (Componenter) -> Component = Componenter::join, consumer: Componenter.() -> Unit): Component =
  ComponentBuilder().apply(consumer).let(joiner)

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
