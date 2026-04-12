@file:Suppress("UnstableApiUsage")

package com.peco2282.devcore.command

import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.peco2282.devcore.command.argument.AngleResolver
import com.peco2282.devcore.command.argument.AxisSet
import com.peco2282.devcore.command.argument.BlockInWorldPredicate
import com.peco2282.devcore.command.argument.ColumnBlockPosition
import com.peco2282.devcore.command.argument.ColumnBlockPositionResolver
import com.peco2282.devcore.command.argument.ColumnFinePosition
import com.peco2282.devcore.command.argument.ColumnFinePositionResolver
import com.peco2282.devcore.command.argument.FinePositionResolver
import com.peco2282.devcore.command.argument.Rotation
import com.peco2282.devcore.command.argument.RotationResolver
import com.peco2282.devcore.command.argument.SlotRange
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.entity.LookAnchor
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.HeightMap
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import java.util.UUID

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
  description: String? = null,
  alias: Collection<String> = emptyList(),
  block: CommandCreator<LiteralArgumentBuilder<CommandSourceStack>>.() -> Unit
) {
  val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
  val creator = CommandCreator(this, builder).apply(block)
  creator.register(description, alias)
}

/**
 * Extension functions for [CommandContext] to retrieve arguments easily.
 */
inline fun <reified T> CommandContext<CommandSourceStack>.getArg(name: String): T =
  getArgument(name, T::class.java)

/**
 * Retrieves a [Player] from the command context.
 */
fun CommandContext<CommandSourceStack>.getPlayer(name: String): Player? =
  getArg<PlayerSelectorArgumentResolver>(name)
    .resolve(source).firstOrNull()

/**
 * Retrieves a list of [Player]s from the command context.
 */
fun CommandContext<CommandSourceStack>.getPlayers(name: String): List<Player> =
  getArg<PlayerSelectorArgumentResolver>(name)
    .resolve(source)

/**
 * Retrieves a [Entity] from the command context.
 */
fun CommandContext<CommandSourceStack>.getEntity(name: String): Entity? =
  getArg<EntitySelectorArgumentResolver>(name)
    .resolve(source).firstOrNull()

/**
 * Retrieves a list of [Entity]s from the command context.
 */
fun CommandContext<CommandSourceStack>.getEntities(name: String): List<Entity> =
  getArg<EntitySelectorArgumentResolver>(name)
    .resolve(source)

/**
 * Retrieves a [Location] (Block Position) from the command context.
 */
fun CommandContext<CommandSourceStack>.getLocation(name: String): Location =
  getArg<BlockPositionResolver>(name)
    .resolve(source).toLocation(source.location.world)

/**
 * Retrieves a [Location] (Fine Position) from the command context.
 */
fun CommandContext<CommandSourceStack>.getFineLocation(name: String): Location =
  getArg<FinePositionResolver>(name)
    .resolve(source).toLocation(source.location.world)

/**
 * Retrieves a [World] from the command context.
 */
fun CommandContext<CommandSourceStack>.getWorld(name: String): World =
  getArg(name)

fun CommandContext<CommandSourceStack>.getRotation(name: String): Rotation =
  getArg<RotationResolver>(name)
    .resolve(source)

fun CommandContext<CommandSourceStack>.getAngle(name: String): Float =
  getArg<AngleResolver>(name)
    .resolve(source)

fun CommandContext<CommandSourceStack>.getColumnBlockPosition(name: String): ColumnBlockPosition =
  getArg<ColumnBlockPositionResolver>(name)
    .resolve(source)

fun CommandContext<CommandSourceStack>.getPlayerProfiles(name: String): Collection<PlayerProfile> =
  getArg<PlayerProfileListResolver>(name)
    .resolve(source)

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
  type: ArgumentType<V>,
  creator: CommandCreator<RequiredArgumentBuilder<CommandSourceStack, V>>.() -> Unit = {}
) = argument(name, type, creator)

/**
 * Retrieves a [ColumnFinePosition] from the command context.
 */
fun CommandContext<CommandSourceStack>.getColumnFinePosition(name: String): ColumnFinePosition =
  getArg<ColumnFinePositionResolver>(name)
    .resolve(source)

/**
 * Retrieves an [AxisSet] from the command context.
 */
fun CommandContext<CommandSourceStack>.getAxes(name: String): AxisSet =
  getArg(name)

/**
 * Retrieves a [BlockInWorldPredicate] from the command context.
 */
fun CommandContext<CommandSourceStack>.getBlockInWorldPredicate(name: String): BlockInWorldPredicate =
  getArg(name)

/**
 * Retrieves a [BlockState] from the command context.
 */
fun CommandContext<CommandSourceStack>.getBlockState(name: String): BlockState =
  getArg(name)

/**
 * Retrieves an [ItemStack] from the command context.
 */
fun CommandContext<CommandSourceStack>.getItemStack(name: String): ItemStack =
  getArg(name)

/**
 * Retrieves an [ItemStackPredicate] from the command context.
 */
fun CommandContext<CommandSourceStack>.getItemStackPredicate(name: String): ItemStackPredicate =
  getArg(name)

/**
 * Retrieves a [NamedTextColor] from the command context.
 */
fun CommandContext<CommandSourceStack>.getNamedColor(name: String): NamedTextColor =
  getArg(name)

/**
 * Retrieves a [TextColor] (hex color) from the command context.
 */
fun CommandContext<CommandSourceStack>.getHexColor(name: String): TextColor =
  getArg(name)

/**
 * Retrieves a [Component] from the command context.
 */
fun CommandContext<CommandSourceStack>.getComponent(name: String): Component =
  getArg(name)

/**
 * Retrieves a [Style] from the command context.
 */
fun CommandContext<CommandSourceStack>.getStyle(name: String): Style =
  getArg(name)

/**
 * Retrieves a [DisplaySlot] from the command context.
 */
fun CommandContext<CommandSourceStack>.getScoreboardDisplaySlot(name: String): DisplaySlot =
  getArg(name)

/**
 * Retrieves a [NamespacedKey] from the command context.
 */
fun CommandContext<CommandSourceStack>.getNamespacedKey(name: String): NamespacedKey =
  getArg(name)

/**
 * Retrieves a [Key] from the command context.
 */
fun CommandContext<CommandSourceStack>.getKey(name: String): Key =
  getArg(name)

/**
 * Retrieves an [IntegerRangeProvider] from the command context.
 */
fun CommandContext<CommandSourceStack>.getIntegerRange(name: String): IntegerRangeProvider =
  getArg(name)

/**
 * Retrieves a [DoubleRangeProvider] from the command context.
 */
fun CommandContext<CommandSourceStack>.getDoubleRange(name: String): DoubleRangeProvider =
  getArg(name)

/**
 * Retrieves a [GameMode] from the command context.
 */
fun CommandContext<CommandSourceStack>.getGameMode(name: String): GameMode =
  getArg(name)

/**
 * Retrieves a [HeightMap] from the command context.
 */
fun CommandContext<CommandSourceStack>.getHeightMap(name: String): HeightMap =
  getArg(name)

/**
 * Retrieves a [UUID] from the command context.
 */
fun CommandContext<CommandSourceStack>.getUuid(name: String): UUID =
  getArg(name)

/**
 * Retrieves a [Criteria] (objective criteria) from the command context.
 */
fun CommandContext<CommandSourceStack>.getObjectiveCriteria(name: String): Criteria =
  getArg(name)

/**
 * Retrieves a [LookAnchor] (entity anchor) from the command context.
 */
fun CommandContext<CommandSourceStack>.getEntityAnchor(name: String): LookAnchor =
  getArg(name)

/**
 * Retrieves a time value in ticks ([Int]) from the command context.
 */
fun CommandContext<CommandSourceStack>.getTime(name: String): Int =
  getArg(name)

/**
 * Retrieves a [Mirror] (template mirror) from the command context.
 */
fun CommandContext<CommandSourceStack>.getTemplateMirror(name: String): Mirror =
  getArg(name)

/**
 * Retrieves a [StructureRotation] (template rotation) from the command context.
 */
fun CommandContext<CommandSourceStack>.getTemplateRotation(name: String): StructureRotation =
  getArg(name)

/**
 * Retrieves a [Team] from the command context.
 */
fun CommandContext<CommandSourceStack>.getTeam(name: String): Team? =
  getArg(name)

/**
 * Retrieves an [Objective] from the command context.
 */
fun CommandContext<CommandSourceStack>.getObjective(name: String): Objective? =
  getArg(name)

/**
 * Retrieves a slot index ([Int]) from the command context.
 */
fun CommandContext<CommandSourceStack>.getSlot(name: String): Int =
  getArg(name)

/**
 * Retrieves a [SlotRange] from the command context.
 */
fun CommandContext<CommandSourceStack>.getSlots(name: String): SlotRange =
  getArg(name)
