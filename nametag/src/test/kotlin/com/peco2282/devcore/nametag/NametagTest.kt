package com.peco2282.devcore.nametag

import be.seeseemelk.mockbukkit.MockBukkit
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NametagTest {
  @BeforeTest fun setup() { MockBukkit.mock() }
  @AfterTest fun tearDown() { MockBukkit.unmock() }

  @Test
  fun `configuration applies to an existing scoreboard without replacing it`() {
    val scoreboard = Bukkit.getScoreboardManager().newScoreboard
    val id = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val config = NametagConfig().apply {
      prefix = Component.text("[VIP] ")
      suffix = Component.text(" ♥")
      color = NamedTextColor.YELLOW
      sortKey = "01-vip"
    }

    applyNametag(scoreboard, id, "Peco", config)
    val team = scoreboard.getEntryTeam("Peco")!!
    assertEquals(Component.text("[VIP] "), team.prefix())
    assertEquals(NamedTextColor.YELLOW, team.color())
    assertEquals(scoreboard, team.scoreboard)
  }

  @Test
  fun `sort keys are constrained to safe team names`() {
    assertEquals(16, teamName("01-vip", UUID.randomUUID()).length)
    assertFailsWith<IllegalArgumentException> { teamName("space is invalid", UUID.randomUUID()) }
  }
}
