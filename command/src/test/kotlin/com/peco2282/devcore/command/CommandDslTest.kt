package com.peco2282.devcore.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CommandDslTest {

  @Test
  fun testDslStructure() {
    val builder = LiteralArgumentBuilder.literal<CommandSourceStack>("test")
    val creator = CommandCreator(null, builder)

    creator.apply {
      permission("test.permission")

      aliases("s1", "s2") {
        executes { 1 }
      }

      "sub" {
        integer("value", min = 1) {
          executesSuspend { 1 }
        }
      }

      // Paper's ArgumentTypes require some registry access which is not available in unit tests.
      // Skipping complex types for now as they cause NoSuchElementException in mock environment.
      /*
      blockPos("pos") {
          executes { 1 }
      }

      singlePlayer("target") {
          executes { 1 }
      }
      */

      suggestionAsync { listOf("a", "b", "c") }

      literal("another") executes {
        it.sendSuccess { text("Done!") }
        1
      }

      executesPlayer { player, context ->
        player.sendMessage("Hello!")
        1
      }
    }

    val node = builder.build()
    assertNotNull(node)
    assertNotNull(node.getChild("sub"))
    assertNotNull(node.getChild("s1"))
    assertNotNull(node.getChild("s2"))
    assertNotNull(node.getChild("another"))
    assertNotNull(node.getChild("sub").getChild("value"))
  }
}
