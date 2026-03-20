package com.peco2282.devcore.world

import org.bukkit.Difficulty
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

@WorldDsl
interface WorldEditor {
  val world: World

  /**
   * サーバーの時刻を設定します。 (0-24000)
   */
  var time: Long

  /**
   * 天気を設定します。
   */
  var weather: WeatherType

  /**
   * ゲームルールを設定します。
   */
  fun <T : Any> gameRule(rule: GameRule<T>, value: T)

  /**
   * スポーン地点を設定します。
   */
  var spawnLocation: Location

  /**
   * 難易度を設定します。
   */
  var difficulty: Difficulty

  /**
   * ワールド内のすべてのプレイヤーを編集します。
   */
  fun players(action: PlayerEditor.() -> Unit)

  /**
   * 条件に一致するプレイヤーを編集します。
   */
  fun players(filter: (Player) -> Boolean, action: PlayerEditor.() -> Unit)

  /**
   * 特定のチャンクを編集します。
   */
  fun chunk(x: Int, z: Int, action: ChunkEditor.() -> Unit)

  /**
   * 特定のブロックを編集します。
   */
  fun block(x: Int, y: Int, z: Int, action: BlockEditor.() -> Unit)

  /**
   * 特定のブロックを編集します。
   */
  fun block(location: Location, action: BlockEditor.() -> Unit)

  /**
   * モブのスポーンを制御します。
   */
  var doMobSpawning: Boolean

  /**
   * 昼夜のサイクルを制御します。
   */
  var doDaylightCycle: Boolean

  /**
   * 天気の変化を制御します。
   */
  var doWeatherCycle: Boolean

  /**
   * 死亡時のインベントリ保持を制御します。
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
 * ワールドの設定をDSLで編集します。
 */
fun World.edit(action: WorldEditor.() -> Unit) {
  WorldEditorImpl(this).apply(action)
}
