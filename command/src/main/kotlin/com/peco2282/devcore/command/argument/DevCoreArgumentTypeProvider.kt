package com.peco2282.devcore.command.argument

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.peco2282.devcore.util.DevCoreInternal
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.entity.LookAnchor
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.HeightMap
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.inventory.ItemStack
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Provider interface for creating various Brigadier argument types used in DevCore commands.
 * 
 * This interface provides factory methods for creating argument types from Brigadier,
 * Minecraft, Bukkit, and Paper APIs, allowing for easy command argument definition.
 */
interface DevCoreArgumentTypeProvider {
  // Bridger Argument-types: start

  /**
   * Creates an integer argument type with no constraints.
   * 
   * @return An [IntegerArgumentType] that accepts any integer value
   */
  fun integer(): IntegerArgumentType = IntegerArgumentType.integer()

  /**
   * Creates an integer argument type with a minimum value constraint.
   * 
   * @param min The minimum allowed value (inclusive)
   * @return An [IntegerArgumentType] that accepts integers >= min
   */
  fun integer(min: Int): IntegerArgumentType = IntegerArgumentType.integer(min)

  /**
   * Creates an integer argument type with minimum and maximum value constraints.
   * 
   * @param min The minimum allowed value (inclusive)
   * @param max The maximum allowed value (inclusive)
   * @return An [IntegerArgumentType] that accepts integers in the range [min, max]
   */
  fun integer(min: Int, max: Int): IntegerArgumentType = IntegerArgumentType.integer(min, max)

  /**
   * Creates a double argument type with no constraints.
   * 
   * @return A [DoubleArgumentType] that accepts any double value
   */
  fun double(): DoubleArgumentType = DoubleArgumentType.doubleArg()

  /**
   * Creates a double argument type with a minimum value constraint.
   * 
   * @param min The minimum allowed value (inclusive)
   * @return A [DoubleArgumentType] that accepts doubles >= min
   */
  fun double(min: Double): DoubleArgumentType = DoubleArgumentType.doubleArg(min)

  /**
   * Creates a double argument type with minimum and maximum value constraints.
   * 
   * @param min The minimum allowed value (inclusive)
   * @param max The maximum allowed value (inclusive)
   * @return A [DoubleArgumentType] that accepts doubles in the range [min, max]
   */
  fun double(min: Double, max: Double): DoubleArgumentType = DoubleArgumentType.doubleArg(min, max)

  /**
   * Creates a float argument type with no constraints.
   * 
   * @return A [FloatArgumentType] that accepts any float value
   */
  fun float(): FloatArgumentType = FloatArgumentType.floatArg()

  /**
   * Creates a float argument type with a minimum value constraint.
   * 
   * @param min The minimum allowed value (inclusive)
   * @return A [FloatArgumentType] that accepts floats >= min
   */
  fun float(min: Float): FloatArgumentType = FloatArgumentType.floatArg(min)

  /**
   * Creates a float argument type with minimum and maximum value constraints.
   * 
   * @param min The minimum allowed value (inclusive)
   * @param max The maximum allowed value (inclusive)
   * @return A [FloatArgumentType] that accepts floats in the range [min, max]
   */
  fun float(min: Float, max: Float): FloatArgumentType = FloatArgumentType.floatArg(min, max)

  /**
   * Creates a long argument type with no constraints.
   * 
   * @return A [LongArgumentType] that accepts any long value
   */
  fun long(): LongArgumentType = LongArgumentType.longArg()

  /**
   * Creates a long argument type with a minimum value constraint.
   * 
   * @param min The minimum allowed value (inclusive)
   * @return A [LongArgumentType] that accepts longs >= min
   */
  fun long(min: Long): LongArgumentType = LongArgumentType.longArg(min)

  /**
   * Creates a long argument type with minimum and maximum value constraints.
   * 
   * @param min The minimum allowed value (inclusive)
   * @param max The maximum allowed value (inclusive)
   * @return A [LongArgumentType] that accepts longs in the range [min, max]
   */
  fun long(min: Long, max: Long): LongArgumentType = LongArgumentType.longArg(min, max)

  /**
   * Creates a boolean argument type.
   * 
   * @return A [BoolArgumentType] that accepts true or false
   */
  fun boolean(): BoolArgumentType = BoolArgumentType.bool()

  /**
   * Creates a string argument type that reads a single quoted or unquoted string.
   * 
   * @return A [StringArgumentType] that reads one string token
   */
  fun string(): StringArgumentType = StringArgumentType.string()

  /**
   * Creates a greedy string argument type that consumes all remaining input.
   * 
   * @return A [StringArgumentType] that reads all remaining text as a single string
   */
  fun greedyString(): StringArgumentType = StringArgumentType.greedyString()

  /**
   * Creates a word argument type that reads a single unquoted word.
   * 
   * @return A [StringArgumentType] that reads one word (no spaces allowed)
   */
  fun word(): StringArgumentType = StringArgumentType.word()
  // Bridger Argument-types: end

  // MC Argument-types: start

  /**
   * Creates a team argument type for selecting scoreboard teams.
   * 
   * @return A [TeamArgumentType] that resolves to a scoreboard team
   */
  fun team(): TeamArgumentType

  /**
   * Creates a slot argument type for inventory slots.
   * 
   * @return A [SlotArgumentType] that represents a single inventory slot
   */
  fun slot(): SlotArgumentType

  /**
   * Creates a slots argument type for multiple inventory slots.
   * 
   * @return A [SlotsArgumentType] that represents a range of inventory slots
   */
  fun slots(): SlotsArgumentType

  /**
   * Creates an objective argument type for scoreboard objectives.
   * 
   * @return An [ObjectiveArgumentType] that resolves to a scoreboard objective
   */
  fun objective(): ObjectiveArgumentType
  // MC Argument-types: end

  // Bukkit & Paper Argument-types: start

  /**
   * Creates an entity selector argument type for selecting a single entity.
   * 
   * @return An [ArgumentType] that resolves to one entity
   */
  fun entity(): ArgumentType<EntitySelectorArgumentResolver> = ArgumentTypes.entity()

  /**
   * Creates a player selector argument type for selecting a single player.
   * 
   * @return An [ArgumentType] that resolves to one player
   */
  fun player(): ArgumentType<PlayerSelectorArgumentResolver> = ArgumentTypes.player()

  /**
   * Creates an entity selector argument type for selecting multiple entities.
   * 
   * @return An [ArgumentType] that resolves to one or more entities
   */
  fun entities(): ArgumentType<EntitySelectorArgumentResolver> = ArgumentTypes.entities()

  /**
   * Creates a player selector argument type for selecting multiple players.
   * 
   * @return An [ArgumentType] that resolves to one or more players
   */
  fun players(): ArgumentType<PlayerSelectorArgumentResolver> = ArgumentTypes.players()

  /**
   * Creates a player profiles argument type for selecting player profiles.
   * 
   * @return An [ArgumentType] that resolves to a list of player profiles
   */
  fun playerProfiles(): ArgumentType<PlayerProfileListResolver> = ArgumentTypes.playerProfiles()

  /**
   * Creates a block position argument type for integer block coordinates.
   * 
   * @return An [ArgumentType] that resolves to a block position
   */
  fun blockPosition(): ArgumentType<BlockPositionResolver> = ArgumentTypes.blockPosition()

  /**
   * Creates a column block position argument type for 2D integer coordinates (X, Z).
   * 
   * @return An [ArgumentType] that resolves to a column position
   */
  fun columnBlockPosition(): ArgumentType<ColumnBlockPositionResolver>

  /**
   * Creates a fine position argument type for precise 3D coordinates.
   * 
   * @param centerIntegers Whether integer coordinates should be centered at 0.5
   * @return An [ArgumentType] that resolves to a precise 3D position
   */
  fun finePosition(centerIntegers: Boolean): ArgumentType<FinePositionResolver>

  /**
   * Creates a column fine position argument type for precise 2D coordinates (X, Z).
   * 
   * @param centerIntegers Whether integer coordinates should be centered at 0.5
   * @return An [ArgumentType] that resolves to a precise 2D position
   */
  fun columnFinePosition(centerIntegers: Boolean): ArgumentType<ColumnFinePositionResolver>

  /**
   * Creates a rotation argument type for yaw and pitch values.
   * 
   * @return An [ArgumentType] that resolves to rotation angles
   */
  fun rotation(): ArgumentType<RotationResolver>

  /**
   * Creates an angle argument type for a single rotation angle.
   * 
   * @return An [ArgumentType] that resolves to a single angle value
   */
  fun angle(): ArgumentType<AngleResolver>

  /**
   * Creates an axes argument type for a set of coordinate axes.
   * 
   * @return An [ArgumentType] that resolves to a set of axes (X, Y, and/or Z)
   */
  fun axes(): ArgumentType<AxisSet>

  /**
   * Creates a block state argument type for block types and states.
   * 
   * @return An [ArgumentType] that resolves to a block state
   */
  fun blockState(): ArgumentType<BlockState> = ArgumentTypes.blockState()

  /**
   * Creates a block predicate argument type for testing blocks in the world.
   * 
   * @return An [ArgumentType] that resolves to a block predicate
   */
  fun blockInWorldPredicate(): ArgumentType<BlockInWorldPredicate>

  /**
   * Creates an item stack argument type for item types and NBT data.
   * 
   * @return An [ArgumentType] that resolves to an item stack
   */
  fun itemStack(): ArgumentType<ItemStack> = ArgumentTypes.itemStack()

  /**
   * Creates an item stack predicate argument type for testing items.
   * 
   * @return An [ArgumentType] that resolves to an item predicate
   */
  fun itemStackPredicate(): ArgumentType<ItemStackPredicate> = ArgumentTypes.itemPredicate()

  /**
   * Creates a named color argument type for standard Minecraft colors.
   * 
   * @return An [ArgumentType] that resolves to a named text color
   */
  fun namedColor(): ArgumentType<NamedTextColor> = ArgumentTypes.namedColor()

  /**
   * Creates a hexadecimal color argument type for RGB colors.
   * 
   * @return An [ArgumentType] that resolves to a hex text color
   */
  fun hexColor(): ArgumentType<TextColor>

  /**
   * Creates a component argument type for formatted text components.
   * 
   * @return An [ArgumentType] that resolves to a text component
   */
  fun component(): ArgumentType<Component> = ArgumentTypes.component()

  /**
   * Creates a style argument type for text formatting styles.
   * 
   * @return An [ArgumentType] that resolves to a text style
   */
  fun style(): ArgumentType<Style> = ArgumentTypes.style()

  /**
   * Creates a signed message argument type for chat messages.
   * 
   * @return An [ArgumentType] that resolves to a signed message
   */
  fun signedMessage(): ArgumentType<SignedMessageResolver> = ArgumentTypes.signedMessage()

  /**
   * Creates a scoreboard display slot argument type.
   * 
   * @return An [ArgumentType] that resolves to a display slot
   */
  fun scoreboardDisplaySlot(): ArgumentType<DisplaySlot> = ArgumentTypes.scoreboardDisplaySlot()

  /**
   * Creates a namespaced key argument type for Bukkit resource identifiers.
   * 
   * @return An [ArgumentType] that resolves to a namespaced key
   */
  fun namespacedKey(): ArgumentType<NamespacedKey> = ArgumentTypes.namespacedKey()

  /**
   * Creates a key argument type for Adventure resource identifiers.
   * 
   * @return An [ArgumentType] that resolves to a key
   */
  fun key(): ArgumentType<Key> = ArgumentTypes.key()

  /**
   * Creates an integer range argument type for numeric ranges.
   * 
   * @return An [ArgumentType] that resolves to an integer range
   */
  fun integerRange(): ArgumentType<IntegerRangeProvider> = ArgumentTypes.integerRange()

  /**
   * Creates a double range argument type for numeric ranges.
   * 
   * @return An [ArgumentType] that resolves to a double range
   */
  fun doubleRange(): ArgumentType<DoubleRangeProvider> = ArgumentTypes.doubleRange()

  /**
   * Creates a world argument type for selecting worlds.
   * 
   * @return An [ArgumentType] that resolves to a world
   */
  fun world(): ArgumentType<World> = ArgumentTypes.world()

  /**
   * Creates a game mode argument type for player game modes.
   * 
   * @return An [ArgumentType] that resolves to a game mode
   */
  fun gameMode(): ArgumentType<GameMode> = ArgumentTypes.gameMode()

  /**
   * Creates a height map argument type for world height maps.
   * 
   * @return An [ArgumentType] that resolves to a height map type
   */
  fun heightMap(): ArgumentType<HeightMap> = ArgumentTypes.heightMap()

  /**
   * Creates a UUID argument type for unique identifiers.
   * 
   * @return An [ArgumentType] that resolves to a UUID
   */
  fun uuid(): ArgumentType<UUID> = ArgumentTypes.uuid()

  /**
   * Creates an objective criteria argument type for scoreboard criteria.
   * 
   * @return An [ArgumentType] that resolves to objective criteria
   */
  fun objectiveCriteria(): ArgumentType<Criteria> = ArgumentTypes.objectiveCriteria()

  /**
   * Creates an entity anchor argument type for look anchor points.
   * 
   * @return An [ArgumentType] that resolves to a look anchor
   */
  fun entityAnchor(): ArgumentType<LookAnchor> = ArgumentTypes.entityAnchor()

  /**
   * Creates a time argument type for tick durations.
   * 
   * @param minTicks The minimum allowed time in ticks
   * @return An [ArgumentType] that resolves to a time value in ticks
   */
  fun time(minTicks: Int): ArgumentType<Int> = ArgumentTypes.time(minTicks)

  /**
   * Creates a template mirror argument type for structure mirroring.
   * 
   * @return An [ArgumentType] that resolves to a mirror mode
   */
  fun templateMirror(): ArgumentType<Mirror> = ArgumentTypes.templateMirror()

  /**
   * Creates a template rotation argument type for structure rotation.
   * 
   * @return An [ArgumentType] that resolves to a rotation mode
   */
  fun templateRotation(): ArgumentType<StructureRotation> = ArgumentTypes.templateRotation()

  /**
   * Creates a resource key argument type for registry entries.
   * 
   * @param T The type of registry entry
   * @param registryKey The registry to use for validation
   * @return An [ArgumentType] that resolves to a typed key
   */
  fun <T> resourceKey(registryKey: RegistryKey<T>): ArgumentType<TypedKey<T>> = ArgumentTypes.resourceKey(registryKey)

  /**
   * Creates a resource argument type for registry values.
   * 
   * @param T The type of registry entry
   * @param registryKey The registry to use for lookup
   * @return An [ArgumentType] that resolves to a registry value
   */
  fun <T> resource(registryKey: RegistryKey<T>): ArgumentType<T> = ArgumentTypes.resource(registryKey)
  // Bukkit & Paper Argument-types: end
}

/**
 * Internal wrapper class for argument types that converts between types.
 * 
 * This class wraps a Brigadier [ArgumentType] and applies a [ResultConverter] to transform
 * the parsed result from type [B] to type [C].
 * 
 * @param B The base type produced by the wrapped argument type
 * @param C The converted type that this argument type produces
 * @param argType The underlying Brigadier argument type to wrap
 * @param converter The converter to transform results from type B to type C
 */
@DevCoreInternal
open class DevCoreArgumentType<B, C>(val argType: ArgumentType<B>, val converter: ResultConverter<B, C>) :
  ArgumentType<C> {
  override fun parse(reader: StringReader): C = converter.convert(argType.parse(reader))

  override fun getExamples(): Collection<String> = argType.examples
  override fun <S : Any> listSuggestions(
    context: CommandContext<S>,
    builder: SuggestionsBuilder
  ): CompletableFuture<Suggestions> {
    return argType.listSuggestions(context, builder)
  }
}
