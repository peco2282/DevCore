package com.peco2282.devcore.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

@DslMarker
annotation class CommandDsl

inline fun Plugin.command(name: String, block: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit) {
  val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
  val creator = CommandCreator(builder)
  creator.block()
  creator.register(this)
}

fun main() {
  val plugin = object : JavaPlugin() {}

  plugin.command("test") {
    requires { it.sender.isOp }
    executes { 1 }
  }
}
