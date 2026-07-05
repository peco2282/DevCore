package com.peco2282.devcore.world

import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * DSL interface for editing Bukkit world properties.
 * 
 * This interface provides a type-safe DSL for modifying world settings,
 * including time, weather, game rules, spawn location, difficulty, and more.
 * 
 * Use the [World.edit] extension function to access this DSL:
 * ```kotlin
 * world.edit {
 *   time = 6000L // Set to noon
 *   weather = WeatherType.CLEAR
 *   difficulty = Difficulty.HARD
 * }
 * ```
 */
@WorldDsl
interface WorldEditor {
  /**
   * The world being edited.
   */
  val world: World

  /**
   * The current time of day in the world (0-24000).
   * 
   * Time values:
   * - 0: Dawn
   * - 6000: Noon
   * - 12000: Dusk
   * - 18000: Midnight
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   time = 6000L // Set to noon
   * }
   * ```
   */
  var time: Long

  /**
   * The current weather type in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   weather = WeatherType.RAIN
   * }
   * ```
   * 
   * @see WeatherType
   */
  var weather: WeatherType

  /**
   * Sets a game rule for the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   gameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
   *   gameRule(GameRule.MAX_ENTITY_CRAMMING, 10)
   * }
   * ```
   * 
   * @param rule The game rule to modify
   * @param value The new value for the game rule
   */
  fun <T : Any> gameRule(rule: GameRule<T>, value: T)

  /**
   * The spawn location for the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   spawnLocation = Location(world, 0.0, 64.0, 0.0)
   * }
   * ```
   */
  var spawnLocation: Location

  /**
   * The difficulty level of the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   difficulty = Difficulty.PEACEFUL
   * }
   * ```
   */
  var difficulty: Difficulty

  /**
   * Applies an action to all players in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   players {
   *     health = 20.0
   *     foodLevel = 20
   *   }
   * }
   * ```
   * 
   * @param action The action to apply to each player
   * @see PlayerEditor
   */
  fun players(action: PlayerEditor.() -> Unit)

  /**
   * Applies an action to filtered players in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   players({ it.isOp }) {
   *     gameMode = GameMode.CREATIVE
   *   }
   * }
   * ```
   * 
   * @param filter A predicate to filter players
   * @param action The action to apply to each filtered player
   * @see PlayerEditor
   */
  fun players(filter: (Player) -> Boolean, action: PlayerEditor.() -> Unit)

  /**
   * Edits a chunk at the specified coordinates.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   chunk(0, 0) {
   *     load()
   *     // Chunk editing operations
   *   }
   * }
   * ```
   * 
   * @param x The chunk X coordinate
   * @param z The chunk Z coordinate
   * @param action The action to apply to the chunk
   * @see ChunkEditor
   */
  fun chunk(x: Int, z: Int, action: ChunkEditor.() -> Unit)

  /**
   * Edits a block at the specified world coordinates.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   block(100, 64, 200) {
   *     type = Material.DIAMOND_BLOCK
   *   }
   * }
   * ```
   * 
   * @param x The block X coordinate
   * @param y The block Y coordinate
   * @param z The block Z coordinate
   * @param action The action to apply to the block
   * @see BlockEditor
   */
  fun block(x: Int, y: Int, z: Int, action: BlockEditor.() -> Unit)

  /**
   * Edits a block at the specified location.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   block(someLocation) {
   *     type = Material.STONE
   *   }
   * }
   * ```
   * 
   * @param location The location of the block
   * @param action The action to apply to the block
   * @see BlockEditor
   */
  fun block(location: Location, action: BlockEditor.() -> Unit)

  /**
   * Controls whether mobs spawn naturally in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   doMobSpawning = false // Disable mob spawning
   * }
   * ```
   */
  var doMobSpawning: Boolean

  /**
   * Controls whether the day-night cycle progresses in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   doDaylightCycle = false // Freeze time
   * }
   * ```
   */
  var doDaylightCycle: Boolean

  /**
   * Controls whether weather changes naturally in the world.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   doWeatherCycle = false // Prevent weather changes
   * }
   * ```
   */
  var doWeatherCycle: Boolean

  /**
   * Controls whether players keep their inventory on death.
   * 
   * Example:
   * ```kotlin
   * world.edit {
   *   keepInventory = true // Players keep items on death
   * }
   * ```
   */
  var keepInventory: Boolean
}

internal class WorldEditorImpl(override val world: World) : WorldEditor {
  override var time: Long
    get() = world.time
    set(value) {
      world.time = value
    }

  override var weather: WeatherType
    get() = when {
      world.isThundering -> WeatherType.THUNDER
      world.hasStorm() -> WeatherType.RAIN
      else -> WeatherType.CLEAR
    }
    set(value) {
      when (value) {
        WeatherType.CLEAR -> {
          world.setStorm(false)
          world.isThundering = false
        }

        WeatherType.RAIN -> {
          world.setStorm(true)
          world.isThundering = false
        }

        WeatherType.THUNDER -> {
          world.setStorm(true)
          world.isThundering = true
        }
      }
    }

  override fun <T : Any> gameRule(rule: GameRule<T>, value: T) {
    world.setGameRule(rule, value)
  }

  override var spawnLocation: Location
    get() = world.spawnLocation
    set(value) {
      world.setSpawnLocation(value)
    }

  override var difficulty: Difficulty
    get() = world.difficulty
    set(value) {
      world.difficulty = value
    }

  override fun players(action: PlayerEditor.() -> Unit) {
    world.players.forEach { PlayerEditorImpl(it).apply(action) }
  }

  override fun players(filter: (Player) -> Boolean, action: PlayerEditor.() -> Unit) {
    world.players.filter(filter).forEach { PlayerEditorImpl(it).apply(action) }
  }

  override fun chunk(x: Int, z: Int, action: ChunkEditor.() -> Unit) {
    ChunkEditorImpl(world.getChunkAt(x, z)).apply(action)
  }

  override fun block(x: Int, y: Int, z: Int, action: BlockEditor.() -> Unit) {
    BlockEditorImpl(world.getBlockAt(x, y, z)).apply(action)
  }

  override fun block(location: Location, action: BlockEditor.() -> Unit) {
    BlockEditorImpl(location.block).apply(action)
  }

  override var doMobSpawning: Boolean
    get() = world.getGameRuleValue(GameRule.DO_MOB_SPAWNING) ?: true
    set(value) {
      world.setGameRule(GameRule.DO_MOB_SPAWNING, value)
    }

  override var doDaylightCycle: Boolean
    get() = world.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE) ?: true
    set(value) {
      world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, value)
    }

  override var doWeatherCycle: Boolean
    get() = world.getGameRuleValue(GameRule.DO_WEATHER_CYCLE) ?: true
    set(value) {
      world.setGameRule(GameRule.DO_WEATHER_CYCLE, value)
    }

  override var keepInventory: Boolean
    get() = world.getGameRuleValue(GameRule.KEEP_INVENTORY) ?: false
    set(value) {
      world.setGameRule(GameRule.KEEP_INVENTORY, value)
    }
}

/**
 * Provides a DSL for editing world properties.
 * 
 * This extension function creates a WorldEditor context for the world,
 * allowing you to modify world settings using a type-safe DSL.
 * 
 * Basic usage:
 * ```kotlin
 * world.edit {
 *   time = 6000L
 *   weather = WeatherType.CLEAR
 *   difficulty = Difficulty.NORMAL
 * }
 * ```
 * 
 * Advanced usage with multiple operations:
 * ```kotlin
 * world.edit {
 *   // Set time and weather
 *   time = 0L
 *   weather = WeatherType.THUNDER
 *   
 *   // Configure game rules
 *   doMobSpawning = false
 *   doDaylightCycle = false
 *   keepInventory = true
 *   
 *   // Edit players
 *   players({ !it.isOp }) {
 *     gameMode = GameMode.ADVENTURE
 *     health = 20.0
 *   }
 *   
 *   // Edit blocks
 *   block(0, 64, 0) {
 *     type = Material.BEDROCK
 *   }
 *   
 *   // Edit chunks
 *   chunk(0, 0) {
 *     load()
 *   }
 * }
 * ```
 * 
 * @param action The DSL block to execute
 * @see WorldEditor
 */
fun World.edit(action: WorldEditor.() -> Unit) {
  WorldEditorImpl(this).apply(action)
}
