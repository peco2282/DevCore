package com.peco2282.devcore.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * Marker annotation for Command DSL.
 *
 * This annotation is used to restrict the scope of DSL methods within [CommandCreator].
 */
@DslMarker
annotation class CommandDsl

/**
 * Creates and registers a new command for the [Plugin].
 *
 * This is the entry point for the command DSL.
 *
 * @param name the name of the command to create
 * @param block the DSL configuration block for the command
 */
inline fun Plugin.command(
  name: String,
  block: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit
) {
  val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
  val creator = CommandCreator(builder)
  creator.block()
  creator.register(this)
}

/**
 * Extension functions for [CommandContext] to retrieve arguments easily.
 */
inline fun <reified T> CommandContext<CommandSourceStack>.getArg(name: String): T =
  getArgument(name, T::class.java)

fun main() {
  val plugin = object : JavaPlugin() {}

  plugin.command("test") {
    requires { it.sender.isOp }
    executes { 1 }
  }
}
