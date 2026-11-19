package com.peco2282.devcore.game

import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.scheduler
import com.peco2282.devcore.scheduler.ticks
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/** One reusable, single-session minigame definition. */
class Minigame private constructor(
  val name: String,
  private val config: MinigameBuilder,
  private val plugin: Plugin?,
) : Listener, AutoCloseable {
  private val participants = linkedSetOf<Player>()
  private val survivors = linkedSetOf<Player>()
  private val playerTeams = linkedMapOf<UUID, String>()
  private var remaining: Duration = Duration.ZERO
  private var ticker: TaskHandle? = null
  private var closed = false

  var state: GameState = GameState.LOBBY
    private set
  val players: Set<Player> get() = participants.toSet()
  val alive: Set<Player> get() = survivors.toSet()

  init {
    config.validate()
    if (plugin != null) {
      plugin.server.pluginManager.registerEvents(this, plugin)
      ticker = plugin.scheduler.timer(20.ticks, 20.ticks, ::tick)
    }
  }

  /** Adds a player when the current state and capacity permit it. */
  fun join(player: Player): Boolean {
    if (closed || state !in setOf(GameState.LOBBY, GameState.COUNTDOWN) || participants.size >= config.maxPlayers) return false
    if (!participants.add(player)) return false
    survivors += player
    assignTeam(player)
    config.lobby.joinAction(player)
    if (state == GameState.LOBBY && participants.size >= config.minPlayers) transition(GameState.COUNTDOWN)
    return true
  }

  /** Removes a player and cancels a countdown that no longer meets its minimum. */
  fun leave(player: Player): Boolean {
    if (!participants.remove(player)) return false
    survivors -= player
    playerTeams -= player.uniqueId
    if (state == GameState.COUNTDOWN && participants.size < config.minPlayers) transition(GameState.LOBBY)
    if (state == GameState.RUNNING && config.running.endPredicate(this)) transition(GameState.ENDING)
    return true
  }

  /** Removes a participant from the alive set without removing them from the session. */
  fun eliminate(player: Player): Boolean = survivors.remove(player)

  fun teamOf(player: Player): String? = playerTeams[player.uniqueId]

  /** Advances the active phase by one second. Public for deterministic game loops and tests. */
  fun tick() {
    if (closed) return
    when (state) {
      GameState.LOBBY -> Unit
      GameState.COUNTDOWN -> {
        if (participants.size < config.minPlayers) transition(GameState.LOBBY)
        else {
          remaining = (remaining - 1.seconds).coerceAtLeast(Duration.ZERO)
          if (remaining == Duration.ZERO) transition(GameState.RUNNING)
        }
      }
      GameState.RUNNING -> {
        if (config.running.endPredicate(this)) { transition(GameState.ENDING); return }
        if (remaining.isFinite()) {
          remaining = (remaining - 1.seconds).coerceAtLeast(Duration.ZERO)
          config.running.tickAction(remaining)
          if (remaining == Duration.ZERO) transition(GameState.ENDING)
        } else config.running.tickAction(remaining)
      }
      GameState.ENDING -> {
        remaining = (remaining - 1.seconds).coerceAtLeast(Duration.ZERO)
        if (remaining == Duration.ZERO) reset()
      }
    }
  }

  /** Performs a legal lifecycle transition, returning false for forbidden transitions. */
  fun transition(next: GameState): Boolean {
    val allowed = when (state) {
      GameState.LOBBY -> setOf(GameState.COUNTDOWN)
      GameState.COUNTDOWN -> setOf(GameState.LOBBY, GameState.RUNNING)
      GameState.RUNNING -> setOf(GameState.ENDING)
      GameState.ENDING -> setOf(GameState.LOBBY)
    }
    if (next !in allowed) return false
    state = next
    when (next) {
      GameState.LOBBY -> remaining = Duration.ZERO
      GameState.COUNTDOWN -> remaining = config.lobby.countdown
      GameState.RUNNING -> {
        remaining = config.running.duration
        survivors.clear(); survivors.addAll(participants)
        config.running.startAction(players)
      }
      GameState.ENDING -> {
        remaining = config.ending.duration
        config.ending.startAction(survivors.singleOrNull())
      }
    }
    return true
  }

  @EventHandler fun onDeath(event: PlayerDeathEvent) {
    if (state != GameState.RUNNING || event.entity !in participants) return
    config.running.deathAction(this, event.entity)
    if (config.running.endPredicate(this)) transition(GameState.ENDING)
  }

  @EventHandler fun onQuit(event: PlayerQuitEvent) { leave(event.player) }
  @EventHandler fun onDisable(event: PluginDisableEvent) { if (event.plugin === plugin) close() }

  override fun close() {
    if (closed) return
    closed = true
    ticker?.cancel(); ticker = null
    HandlerList.unregisterAll(this)
    participants.clear(); survivors.clear(); playerTeams.clear()
  }

  private fun reset() {
    participants.clear(); survivors.clear(); playerTeams.clear()
    transition(GameState.LOBBY)
  }

  private fun assignTeam(player: Player) {
    if (config.teamNames.isEmpty()) return
    val counts = config.teamNames.associateWith { team -> playerTeams.values.count { it == team } }
    playerTeams[player.uniqueId] = config.teamNames.minBy { counts.getValue(it) }
  }

  companion object {
    internal fun forTesting(name: String, block: MinigameBuilder.() -> Unit): Minigame =
      Minigame(name, MinigameBuilder().apply(block), null)

    internal fun create(plugin: Plugin, name: String, block: MinigameBuilder.() -> Unit): Minigame {
      require(name.isNotBlank()) { "Game name must not be blank" }
      return Minigame(name, MinigameBuilder().apply(block), plugin)
    }
  }
}

/** Creates a lifecycle-bound minigame owned by this plugin. */
fun Plugin.minigame(name: String, block: MinigameBuilder.() -> Unit): Minigame = Minigame.create(this, name, block)
