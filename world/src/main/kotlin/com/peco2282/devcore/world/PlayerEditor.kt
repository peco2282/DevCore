package com.peco2282.devcore.world

import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * DSL interface for editing player properties in a fluent and type-safe manner.
 *
 * This interface provides a convenient way to modify various player attributes
 * such as game mode, health, food level, experience, and more using Kotlin's DSL capabilities.
 *
 * Example usage:
 * ```kotlin
 * player.edit {
 *   gameMode = GameMode.CREATIVE
 *   health = 20.0
 *   foodLevel = 20
 *   level = 10
 *   exp = 0.5f
 *   sendMessage("Welcome to the game!")
 *   teleport(spawnLocation)
 * }
 * ```
 *
 * @see edit
 */
@WorldDsl
interface PlayerEditor {
  /**
   * The player being edited.
   */
  val player: Player

  /**
   * The player's current game mode.
   *
   * Can be set to any of the following:
   * - [GameMode.SURVIVAL]
   * - [GameMode.CREATIVE]
   * - [GameMode.ADVENTURE]
   * - [GameMode.SPECTATOR]
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   gameMode = GameMode.CREATIVE
   * }
   * ```
   */
  var gameMode: GameMode

  /**
   * The player's health value.
   *
   * Valid range is typically 0.0 to 20.0 (where 20.0 is full health).
   * Setting health to 0.0 or below will kill the player.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   health = 20.0 // Full health
   * }
   * ```
   */
  var health: Double

  /**
   * The player's food level.
   *
   * Valid range is 0 to 20 (where 20 is fully fed).
   * Lower values will cause the player to lose health over time.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   foodLevel = 20 // Fully fed
   * }
   * ```
   */
  var foodLevel: Int

  /**
   * The player's experience progress towards the next level.
   *
   * Valid range is 0.0f to 1.0f (where 1.0f represents 100% progress).
   * When set to 1.0f or above, the player will level up.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   exp = 0.5f // 50% progress to next level
   * }
   * ```
   */
  var exp: Float

  /**
   * The player's experience level.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   level = 30 // Set to level 30
   * }
   * ```
   */
  var level: Int

  /**
   * The player's inventory.
   *
   * Provides direct access to the player's inventory for item manipulation.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   inventory.clear()
   *   inventory.addItem(ItemStack(Material.DIAMOND_SWORD))
   * }
   * ```
   */
  val inventory: Inventory

  /**
   * Sends an Adventure [Component] message to the player.
   *
   * Use this method for rich text formatting with colors, hover effects, and click events.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   sendMessage(Component.text("Hello!", NamedTextColor.GOLD))
   * }
   * ```
   *
   * @param message The Adventure Component to send.
   */
  fun sendMessage(message: Component)

  /**
   * Sends a plain text message to the player.
   *
   * The message is automatically converted to an Adventure Component.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   sendMessage("Welcome to the server!")
   * }
   * ```
   *
   * @param message The plain text message to send.
   */
  fun sendMessage(message: String)

  /**
   * Teleports the player to the specified location.
   *
   * Example:
   * ```kotlin
   * player.edit {
   *   teleport(Location(world, 0.0, 64.0, 0.0))
   * }
   * ```
   *
   * @param location The target location.
   */
  fun teleport(location: Location)
}

internal class PlayerEditorImpl(override val player: Player) : PlayerEditor {
  override var gameMode: GameMode
    get() = player.gameMode
    set(value) {
      player.gameMode = value
    }

  override var health: Double
    get() = player.health
    set(value) {
      player.health = value
    }

  override var foodLevel: Int
    get() = player.foodLevel
    set(value) {
      player.foodLevel = value
    }

  override var exp: Float
    get() = player.exp
    set(value) {
      player.exp = value
    }

  override var level: Int
    get() = player.level
    set(value) {
      player.level = value
    }

  override val inventory: Inventory
    get() = player.inventory

  override fun sendMessage(message: Component) {
    player.sendMessage(message)
  }

  override fun sendMessage(message: String) {
    player.sendMessage(Component.text(message))
  }

  override fun teleport(location: Location) {
    player.teleport(location)
  }
}

/**
 * Extension function to edit player properties using a DSL.
 *
 * This function provides a convenient way to modify multiple player attributes
 * in a single, readable block of code.
 *
 * Example usage:
 * ```kotlin
 * // Simple property changes
 * player.edit {
 *   gameMode = GameMode.CREATIVE
 *   health = 20.0
 *   foodLevel = 20
 * }
 *
 * // Complex scenario: Setup a player for a minigame
 * player.edit {
 *   gameMode = GameMode.ADVENTURE
 *   health = 20.0
 *   foodLevel = 20
 *   level = 0
 *   exp = 0.0f
 *   inventory.clear()
 *   inventory.addItem(ItemStack(Material.WOODEN_SWORD))
 *   sendMessage(Component.text("Game starting!", NamedTextColor.GREEN))
 *   teleport(arenaSpawnLocation)
 * }
 *
 * // Give rewards after quest completion
 * player.edit {
 *   level = level + 5
 *   exp = 0.0f
 *   inventory.addItem(ItemStack(Material.DIAMOND, 10))
 *   sendMessage("Quest completed! +5 levels and 10 diamonds!")
 * }
 * ```
 *
 * @param action The DSL action to perform on the player.
 * @receiver The player to edit.
 * @see PlayerEditor
 */
fun Player.edit(action: PlayerEditor.() -> Unit) {
  PlayerEditorImpl(this).apply(action)
}
