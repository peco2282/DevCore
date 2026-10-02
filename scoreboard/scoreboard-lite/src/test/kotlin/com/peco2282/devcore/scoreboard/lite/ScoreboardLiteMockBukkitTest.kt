package com.peco2282.devcore.scoreboard.lite

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.peco2282.devcore.scoreboard.api.ScoreboardApi
import com.peco2282.devcore.scoreboard.api.sidebar
import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.DisplaySlot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ScoreboardLiteMockBukkitTest {
  private lateinit var server: ServerMock

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
    ScoreboardApi.init(MockBukkit.createMockPlugin(), ScoreboardLite)
  }

  @AfterEach
  fun tearDown() {
    ScoreboardApi.destroyAll()
    MockBukkit.unmock()
  }

  @Test
  fun `sidebar show assigns scoreboard with title and lines`() {
    val player = server.addPlayer("Peco")
    val main = server.scoreboardManager.mainScoreboard
    val handle = sidebar(Component.text("DevCore")) {
      line(Component.text("first"))
      line { p -> Component.text("player=${p.name}") }
    }

    handle.show(player)

    assertNotSame(main, player.scoreboard)
    val objective = player.scoreboard.getObjective(DisplaySlot.SIDEBAR)!!
    assertEquals(Component.text("DevCore"), objective.displayName())
    assertEquals(1, objective.getScore("first").score)
    assertEquals(0, objective.getScore("player=Peco").score)
  }

  @Test
  fun `sidebar update renders current dynamic content`() {
    val player = server.addPlayer("Peco")
    var value = "before"
    val dynamicLine: () -> Component = { Component.text(value) }
    val handle = sidebar(Component.text("Dynamic")) {
      line(dynamicLine)
    }
    handle.show(player)

    value = "after"
    handle.update()

    val objective = player.scoreboard.getObjective(DisplaySlot.SIDEBAR)!!
    assertTrue(objective.getScore("after").isScoreSet)
  }

  @Test
  fun `sidebar hide restores main scoreboard`() {
    val player = server.addPlayer()
    val main = server.scoreboardManager.mainScoreboard
    val handle = sidebar(Component.text("Temporary")) {}
    handle.show(player)

    handle.hide(player)

    assertSame(main, player.scoreboard)
  }
}
