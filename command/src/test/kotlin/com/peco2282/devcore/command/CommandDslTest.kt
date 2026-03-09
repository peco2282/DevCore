package com.peco2282.devcore.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CommandDslTest {

    @Test
    fun testDslStructure() {
        val builder = LiteralArgumentBuilder.literal<CommandSourceStack>("test")
        val creator = CommandCreator(builder)

        creator.apply {
            permission("test.permission")
            
            aliases("s1", "s2") {
                executes { 1 }
            }
            
            literal("sub") {
                integer("value", min = 1) {
                    executes { 1 }
                }
            }
            
            literal("another") executes { 1 }
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
