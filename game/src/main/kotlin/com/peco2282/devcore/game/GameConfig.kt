package com.peco2282.devcore.game

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.bukkit.entity.Player

/** Strategy used when assigning players to declared teams. */
enum class Assignment { BALANCED }

/** Stable externally visible lifecycle states of a minigame. */
enum class GameState { LOBBY, COUNTDOWN, RUNNING, ENDING }

class LobbyBuilder {
  var countdown: Duration = 15.seconds
  internal var joinAction: (Player) -> Unit = {}
  fun onJoin(action: (Player) -> Unit) { joinAction = action }
}

class RunningBuilder {
  var duration: Duration = Duration.INFINITE
  internal var startAction: (Set<Player>) -> Unit = {}
  internal var tickAction: (Duration) -> Unit = {}
  internal var deathAction: Minigame.(Player) -> Unit = { eliminate(it) }
  internal var endPredicate: Minigame.() -> Boolean = { false }
  fun onStart(action: (Set<Player>) -> Unit) { startAction = action }
  fun onTick(action: (Duration) -> Unit) { tickAction = action }
  fun onPlayerDeath(action: Minigame.(Player) -> Unit) { deathAction = action }
  fun endWhen(predicate: Minigame.() -> Boolean) { endPredicate = predicate }
}

class EndingBuilder {
  var duration: Duration = 10.seconds
  internal var startAction: (Player?) -> Unit = {}
  fun onStart(action: (Player?) -> Unit) { startAction = action }
}

class TeamsBuilder { var assign: Assignment = Assignment.BALANCED }

/** Builder for [Minigame] definitions. */
class MinigameBuilder {
  var minPlayers: Int = 1
  var maxPlayers: Int = Int.MAX_VALUE
  internal val lobby = LobbyBuilder()
  internal val running = RunningBuilder()
  internal val ending = EndingBuilder()
  internal var teamNames: List<String> = emptyList()
  internal var assignment: Assignment = Assignment.BALANCED

  fun lobby(block: LobbyBuilder.() -> Unit) { lobby.apply(block) }
  fun running(block: RunningBuilder.() -> Unit) { running.apply(block) }
  fun ending(block: EndingBuilder.() -> Unit) { ending.apply(block) }
  fun teams(vararg names: String, block: TeamsBuilder.() -> Unit = {}) {
    require(names.isNotEmpty()) { "At least one team is required" }
    require(names.all(String::isNotBlank) && names.toSet().size == names.size) { "Team names must be unique and non-blank" }
    teamNames = names.toList()
    assignment = TeamsBuilder().apply(block).assign
  }

  internal fun validate() {
    require(minPlayers > 0) { "minPlayers must be positive" }
    require(maxPlayers >= minPlayers) { "maxPlayers must be at least minPlayers" }
    require(!lobby.countdown.isNegative() && lobby.countdown.isFinite()) { "Countdown must be finite and non-negative" }
    require(!running.duration.isNegative()) { "Running duration must be non-negative" }
    require(!ending.duration.isNegative() && ending.duration.isFinite()) { "Ending duration must be finite and non-negative" }
  }
}
