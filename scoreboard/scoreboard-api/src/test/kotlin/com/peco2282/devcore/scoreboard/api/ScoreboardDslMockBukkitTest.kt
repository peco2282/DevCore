package com.peco2282.devcore.scoreboard.api

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.ServerMock
import com.peco2282.devcore.scoreboard.api.builder.LinesBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ScoreboardDslMockBukkitTest {
  private lateinit var server: ServerMock

  @BeforeEach
  fun setUp() {
    server = MockBukkit.mock()
  }

  @AfterEach
  fun tearDown() {
    ScoreboardApi.destroyAll()
    MockBukkit.unmock()
  }

  @Test
  fun `team creates a personal scoreboard and configures membership`() {
    val player = server.addPlayer("Peco")

    player.team("admins") {
      prefix(Component.text("[Admin]", NamedTextColor.RED))
    }

    val team = player.scoreboard.getTeam("admins")
    assertTrue(team != null)
    assertTrue(team.hasEntry("Peco"))
    assertEquals(Component.text("[Admin]", NamedTextColor.RED), team.prefix())
  }

  @Test
  fun `team reuses the players existing non-main scoreboard`() {
    val player = server.addPlayer("Peco")
    val scoreboard = server.scoreboardManager.newScoreboard
    player.scoreboard = scoreboard

    player.team("members") { suffix(Component.text("!")) }

    assertSame(scoreboard, player.scoreboard)
    assertTrue(scoreboard.getTeam("members")!!.hasEntry("Peco"))
  }

  @Test
  fun `lines builder preserves static dynamic and player lines`() {
    val player = server.addPlayer("Peco")
    var dynamic = "first"
    val lines = LinesBuilder().apply {
      +"static"
      +{ dynamic }
      +{ p: org.bukkit.entity.Player -> "player=${p.name}" }
    }.build()

    dynamic = "updated"

    assertEquals(Component.text("static"), lines[0](player))
    assertEquals(Component.text("updated"), lines[1](player))
    assertEquals(Component.text("player=Peco"), lines[2](player))
  }
}
