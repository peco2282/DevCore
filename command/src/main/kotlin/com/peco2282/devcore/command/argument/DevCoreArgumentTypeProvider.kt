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
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.peco2282.devcore.command.argument.DevCoreArgumentTypeProvider.ResultConverter
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

interface DevCoreArgumentTypeProvider {
  // Bridger Argument-types: start
  fun integer(): IntegerArgumentType = IntegerArgumentType.integer()
  fun integer(min: Int): IntegerArgumentType = IntegerArgumentType.integer(min)
  fun integer(min: Int, max: Int): IntegerArgumentType = IntegerArgumentType.integer(min, max)
  fun double(): DoubleArgumentType = DoubleArgumentType.doubleArg()
  fun double(min: Double): DoubleArgumentType = DoubleArgumentType.doubleArg(min)
  fun double(min: Double, max: Double): DoubleArgumentType = DoubleArgumentType.doubleArg(min, max)
  fun float(): FloatArgumentType = FloatArgumentType.floatArg()
  fun float(min: Float): FloatArgumentType = FloatArgumentType.floatArg(min)
  fun float(min: Float, max: Float): FloatArgumentType = FloatArgumentType.floatArg(min, max)
  fun long(): LongArgumentType = LongArgumentType.longArg()
  fun long(min: Long): LongArgumentType = LongArgumentType.longArg(min)
  fun long(min: Long, max: Long): LongArgumentType = LongArgumentType.longArg(min, max)
  fun boolean(): BoolArgumentType = BoolArgumentType.bool()
  fun string(): StringArgumentType = StringArgumentType.string()
  fun greedyString(): StringArgumentType = StringArgumentType.greedyString()
  fun word(): StringArgumentType = StringArgumentType.word()
  // Bukkit Argument-types: end

  fun entity(): ArgumentType<EntitySelectorArgumentResolver> = ArgumentTypes.entity()

  fun player(): ArgumentType<PlayerSelectorArgumentResolver> = ArgumentTypes.player()

  fun entities(): ArgumentType<EntitySelectorArgumentResolver> = ArgumentTypes.entities()

  fun players(): ArgumentType<PlayerSelectorArgumentResolver> = ArgumentTypes.players()

  fun playerProfiles(): ArgumentType<PlayerProfileListResolver> = ArgumentTypes.playerProfiles()

  fun blockPosition(): ArgumentType<BlockPositionResolver> = ArgumentTypes.blockPosition()

  fun columnBlockPosition(): ArgumentType<ColumnBlockPositionResolver>

  fun finePosition(centerIntegers: Boolean): ArgumentType<FinePositionResolver>

  fun columnFinePosition(centerIntegers: Boolean): ArgumentType<ColumnFinePositionResolver>

  fun rotation(): ArgumentType<RotationResolver>

  fun angle(): ArgumentType<AngleResolver>

  fun axes(): ArgumentType<AxisSet>

  fun blockState(): ArgumentType<BlockState> = ArgumentTypes.blockState()

  fun blockInWorldPredicate(): ArgumentType<BlockInWorldPredicate>

  fun itemStack(): ArgumentType<ItemStack> = ArgumentTypes.itemStack()

  fun itemStackPredicate(): ArgumentType<ItemStackPredicate> = ArgumentTypes.itemPredicate()

  fun namedColor(): ArgumentType<NamedTextColor> = ArgumentTypes.namedColor()

  fun hexColor(): ArgumentType<TextColor>

  fun component(): ArgumentType<Component> = ArgumentTypes.component()

  fun style(): ArgumentType<Style> = ArgumentTypes.style()

  fun signedMessage(): ArgumentType<SignedMessageResolver> = ArgumentTypes.signedMessage()

  fun scoreboardDisplaySlot(): ArgumentType<DisplaySlot> = ArgumentTypes.scoreboardDisplaySlot()

  fun namespacedKey(): ArgumentType<NamespacedKey> = ArgumentTypes.namespacedKey()

  fun key(): ArgumentType<Key> = ArgumentTypes.key()

  fun integerRange(): ArgumentType<IntegerRangeProvider> = ArgumentTypes.integerRange()

  fun doubleRange(): ArgumentType<DoubleRangeProvider> = ArgumentTypes.doubleRange()

  fun world(): ArgumentType<World> = ArgumentTypes.world()

  fun gameMode(): ArgumentType<GameMode> = ArgumentTypes.gameMode()

  fun heightMap(): ArgumentType<HeightMap> = ArgumentTypes.heightMap()

  fun uuid(): ArgumentType<UUID> = ArgumentTypes.uuid()

  fun objectiveCriteria(): ArgumentType<Criteria> = ArgumentTypes.objectiveCriteria()

  fun entityAnchor(): ArgumentType<LookAnchor> = ArgumentTypes.entityAnchor()

  fun time(minTicks: Int): ArgumentType<Int> = ArgumentTypes.time(minTicks)

  fun templateMirror(): ArgumentType<Mirror> = ArgumentTypes.templateMirror()

  fun templateRotation(): ArgumentType<StructureRotation> = ArgumentTypes.templateRotation()

  fun <T> resourceKey(registryKey: RegistryKey<T>): ArgumentType<TypedKey<T>> = ArgumentTypes.resourceKey(registryKey)

  fun <T> resource(registryKey: RegistryKey<T>): ArgumentType<T> = ArgumentTypes.resource(registryKey)

  fun interface ResultConverter<T, R> {
    @Throws(CommandSyntaxException::class)
    fun convert(var1: T): R
  }

}

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
