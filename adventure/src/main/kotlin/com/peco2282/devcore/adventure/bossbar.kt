package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

/**
 * DSL builder for configuring and creating Adventure [BossBar] instances.
 *
 * Provides properties and helper functions to configure the name, progress,
 * color, overlay, and flags of a boss bar.
 *
 * Example usage:
 * ```kotlin
 * val bar = bossBar {
 *   name { text("Boss Health") }
 *   progress = 0.75f
 *   color = BossBar.Color.RED
 *   overlay = BossBar.Overlay.NOTCHED_10
 *   flag(BossBar.Flag.DARKEN_SCREEN)
 * }
 * ```
 */
@BossBarDsl
class BossBarBuilder {
  /**
   * The display name component of the boss bar.
   *
   * Defaults to [Component.empty].
   */
  var name: Component = Component.empty()

  /**
   * The progress value of the boss bar, ranging from `0.0f` (empty) to `1.0f` (full).
   *
   * Defaults to `1.0f`.
   * Must be finite and within `0.0f..1.0f`.
   */
  var progress: Float = 1.0f

  /**
   * The color of the boss bar.
   *
   * Defaults to [BossBar.Color.PINK].
   */
  var color: BossBar.Color = BossBar.Color.PINK

  /**
   * The overlay style of the boss bar (e.g., solid progress or notched sections).
   *
   * Defaults to [BossBar.Overlay.PROGRESS].
   */
  var overlay: BossBar.Overlay = BossBar.Overlay.PROGRESS

  /**
   * The set of active [BossBar.Flag]s on the boss bar.
   *
   * Defaults to an empty set.
   */
  var flags: Set<BossBar.Flag> = emptySet()

  /**
   * Configures the [name] of the boss bar using the [ComponentBuilder] DSL.
   *
   * @param consumer A lambda configuring the component builder for the boss bar name
   */
  fun name(consumer: ComponentBuilder.() -> Unit) {
    name = component(consumer)
  }

  /**
   * Adds or removes a [BossBar.Flag] from this boss bar.
   *
   * @param flag The flag to modify
   * @param enabled `true` to enable the flag, `false` to remove it
   */
  fun flag(flag: BossBar.Flag, enabled: Boolean = true) {
    flags = if (enabled) flags + flag else flags - flag
  }

  /**
   * Builds and returns a new [BossBar] instance with the configured properties.
   *
   * @return A newly created [BossBar]
   * @throws IllegalArgumentException if [progress] is not finite or not between 0.0 and 1.0
   */
  internal fun build(): BossBar {
    require(progress.isFinite() && progress in 0.0f..1.0f) { "Boss bar progress must be finite and between 0 and 1" }
    return BossBar.bossBar(name, progress, color, overlay, flags)
  }
}

/**
 * Creates a new [BossBar] using the provided builder DSL.
 *
 * @param builder A lambda configuring the [BossBarBuilder]
 * @return The constructed [BossBar] instance
 *
 * Example usage:
 * ```kotlin
 * val bar = bossBar {
 *   name { text("Event Progress") }
 *   progress = 0.5f
 *   color = BossBar.Color.BLUE
 * }
 * ```
 */
fun bossBar(builder: BossBarBuilder.() -> Unit): BossBar = BossBarBuilder().apply(builder).build()

/**
 * Updates this [BossBar] with properties configured in the provided builder DSL.
 *
 * Existing properties are initialized into the builder and any modified values
 * (name, progress, color, overlay, flags) are applied back to this [BossBar].
 *
 * @param builder A lambda configuring the updates via [BossBarBuilder]
 * @return This [BossBar] instance for chaining
 *
 * Example usage:
 * ```kotlin
 * bar.update {
 *   progress = 0.75f
 *   color = BossBar.Color.GREEN
 * }
 * ```
 */
fun BossBar.update(builder: BossBarBuilder.() -> Unit): BossBar {
  val updated = BossBarBuilder().apply {
    name = this@update.name()
    progress = this@update.progress()
    color = this@update.color()
    overlay = this@update.overlay()
    flags = this@update.flags()
  }.apply(builder).build()

  name(updated.name())
  progress(updated.progress())
  color(updated.color())
  overlay(updated.overlay())

  val oldFlags = flags()
  val newFlags = updated.flags()
  (oldFlags - newFlags).forEach { removeFlag(it) }
  (newFlags - oldFlags).forEach { addFlag(it) }
  return this
}

/**
 * Creates a [BossBar] using the provided builder DSL and displays it to this [Audience].
 *
 * @param builder A lambda configuring the [BossBarBuilder]
 * @return The created and displayed [BossBar] instance
 *
 * Example usage:
 * ```kotlin
 * val bar = player.showBossBar {
 *   name { text("Welcome!") }
 *   progress = 1.0f
 *   color = BossBar.Color.PURPLE
 * }
 * ```
 */
fun Audience.showBossBar(builder: BossBarBuilder.() -> Unit): BossBar = bossBar(builder).also { showBossBar(it) }

/**
 * Displays an existing [BossBar] to all audiences in this collection.
 *
 * @param bossBar The boss bar to display
 */
fun Collection<Audience>.showBossBar(bossBar: BossBar) = forEach { it.showBossBar(bossBar) }

/**
 * Creates a [BossBar] using the provided builder DSL and displays it to all audiences in this collection.
 *
 * @param builder A lambda configuring the [BossBarBuilder]
 * @return The created and displayed [BossBar] instance
 *
 * Example usage:
 * ```kotlin
 * val bar = onlinePlayers.showBossBar {
 *   name { text("Server Event") }
 *   progress = 0.5f
 *   color = BossBar.Color.YELLOW
 * }
 * ```
 */
fun Collection<Audience>.showBossBar(builder: BossBarBuilder.() -> Unit): BossBar {
  val bossBar = bossBar(builder)
  forEach { it.showBossBar(bossBar) }
  return bossBar
}

/**
 * Hides an existing [BossBar] from all audiences in this collection.
 *
 * @param bossBar The boss bar to hide
 */
fun Collection<Audience>.hideBossBar(bossBar: BossBar) = forEach { it.hideBossBar(bossBar) }