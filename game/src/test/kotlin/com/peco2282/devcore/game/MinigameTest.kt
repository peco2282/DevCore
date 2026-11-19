package com.peco2282.devcore.game

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import org.bukkit.entity.Player

class MinigameTest {
  private lateinit var server: ServerMock

  @BeforeTest fun setUp() { server = MockBukkit.mock() }
  @AfterTest fun tearDown() { MockBukkit.unmock() }

  @Test fun `countdown starts at minimum and reaches running`() {
    val starts = mutableListOf<List<Player>>()
    val game = testGame {
      minPlayers = 2
      lobby { countdown = 2.seconds }
      running { duration = 3.seconds; onStart { starts += it.toList() } }
    }
    val first = server.addPlayer()
    val second = server.addPlayer()
    assertTrue(game.join(first)); assertEquals(GameState.LOBBY, game.state)
    assertTrue(game.join(second)); assertEquals(GameState.COUNTDOWN, game.state)
    game.tick(); assertEquals(GameState.COUNTDOWN, game.state)
    game.tick(); assertEquals(GameState.RUNNING, game.state)
    assertEquals(listOf(first, second), starts.single())
  }

  @Test fun `leaving below minimum cancels countdown`() {
    val game = testGame { minPlayers = 2; lobby { countdown = 5.seconds } }
    val first = server.addPlayer(); val second = server.addPlayer()
    game.join(first); game.join(second)
    assertTrue(game.leave(second))
    assertEquals(GameState.LOBBY, game.state)
    assertEquals(setOf(first), game.players)
  }

  @Test fun `running predicate ends and ending resets session`() {
    var endingWinner: Player? = null
    val game = testGame {
      minPlayers = 1
      lobby { countdown = 1.seconds }
      running { duration = 30.seconds; endWhen { alive.size <= 1 } }
      ending { duration = 2.seconds; onStart { endingWinner = it } }
    }
    val player = server.addPlayer()
    game.join(player); game.tick()
    assertEquals(GameState.RUNNING, game.state)
    game.tick()
    assertEquals(GameState.ENDING, game.state)
    assertEquals(player, endingWinner)
    game.tick(); game.tick()
    assertEquals(GameState.LOBBY, game.state)
    assertTrue(game.players.isEmpty())
  }

  @Test fun `join constraints and legal transitions are enforced`() {
    val game = testGame { minPlayers = 1; maxPlayers = 1; lobby { countdown = 1.seconds } }
    val first = server.addPlayer(); val second = server.addPlayer()
    assertTrue(game.join(first)); assertFalse(game.join(second))
    game.tick(); assertEquals(GameState.RUNNING, game.state)
    assertFalse(game.join(second))
    assertFalse(game.transition(GameState.LOBBY))
  }

  @Test fun `balanced teams choose smallest then declaration order`() {
    val game = testGame { minPlayers = 10; teams("red", "blue") { assign = Assignment.BALANCED } }
    val players = List(5) { server.addPlayer() }
    players.forEach(game::join)
    assertEquals(listOf("red", "blue", "red", "blue", "red"), players.map { game.teamOf(it) })
  }

  private fun testGame(block: MinigameBuilder.() -> Unit): Minigame =
    Minigame.forTesting("test", block)
}
