@file:Suppress("UnstableApiUsage")

package com.peco2282.devcore.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.Plugin

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
  name: String = this.name.lowercase(),
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

/**
 * Retrieves a [org.bukkit.entity.Player] from the command context.
 */
fun CommandContext<CommandSourceStack>.getPlayer(name: String): org.bukkit.entity.Player? =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver>(name)
    .resolve(source).firstOrNull()

/**
 * Retrieves a list of [org.bukkit.entity.Player]s from the command context.
 */
fun CommandContext<CommandSourceStack>.getPlayers(name: String): List<org.bukkit.entity.Player> =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver>(name)
    .resolve(source)

/**
 * Retrieves a [org.bukkit.entity.Entity] from the command context.
 */
fun CommandContext<CommandSourceStack>.getEntity(name: String): org.bukkit.entity.Entity? =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver>(name)
    .resolve(source).firstOrNull()

/**
 * Retrieves a list of [org.bukkit.entity.Entity]s from the command context.
 */
fun CommandContext<CommandSourceStack>.getEntities(name: String): List<org.bukkit.entity.Entity> =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver>(name)
    .resolve(source)

/**
 * Retrieves a [org.bukkit.Location] (Block Position) from the command context.
 */
fun CommandContext<CommandSourceStack>.getLocation(name: String): org.bukkit.Location =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver>(name)
    .resolve(source).toLocation(source.location.world)

/**
 * Retrieves a [org.bukkit.Location] (Fine Position) from the command context.
 */
fun CommandContext<CommandSourceStack>.getFineLocation(name: String): org.bukkit.Location =
  getArg<io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver>(name)
    .resolve(source).toLocation(source.location.world)

/**
 * Retrieves a [org.bukkit.World] from the command context.
 */
fun CommandContext<CommandSourceStack>.getWorld(name: String): org.bukkit.World =
  getArg(name)

/**
 * Registers a custom argument type to the DSL.
 *
 * This function can be used to add support for custom data types in the command DSL.
 *
 * @param name the name of the argument
 * @param type the Brigadier [com.mojang.brigadier.arguments.ArgumentType]
 * @param creator the configuration block for the command structure
 */
fun <V> CommandCreator<*>.custom(
  name: String,
  type: com.mojang.brigadier.arguments.ArgumentType<V>,
  creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, V>>.() -> Unit = {}
) = argument(name, type, creator)
