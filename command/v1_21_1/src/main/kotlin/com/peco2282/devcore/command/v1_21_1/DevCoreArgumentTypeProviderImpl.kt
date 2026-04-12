package com.peco2282.devcore.command.v1_21_1

import com.google.common.collect.ForwardingSet
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.peco2282.devcore.command.argument.*
import com.peco2282.devcore.command.argument.ResultConverter
import io.papermc.paper.command.brigadier.PaperCommands
import io.papermc.paper.util.MCUtil
import it.unimi.dsi.fastutil.ints.IntList
import net.kyori.adventure.text.format.TextColor
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.AngleArgument
import net.minecraft.commands.arguments.ObjectiveArgument
import net.minecraft.commands.arguments.OperationArgument
import net.minecraft.commands.arguments.SlotArgument
import net.minecraft.commands.arguments.SlotsArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument
import net.minecraft.commands.arguments.coordinates.*
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.state.pattern.BlockInWorld
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.util.CraftLocation
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import java.util.*
import java.util.concurrent.CompletableFuture

class DevCoreArgumentTypeProviderImpl : DevCoreArgumentTypeProvider {
  override fun columnBlockPosition(): ArgumentType<ColumnBlockPositionResolver> = this.wrap(
    ColumnPosArgument.columnPos()
  ) {
    ColumnBlockPositionResolver { sourceStack ->
      val pos = it.getBlockPos(sourceStack as CommandSourceStack)
      Impl.ColumnBlockPositionImpl(pos.x, pos.z)
    }
  }

  override fun finePosition(centerIntegers: Boolean): ArgumentType<FinePositionResolver> = wrap(
    Vec3Argument.vec3(centerIntegers),
  ) {
    FinePositionResolver { sourceStack ->
      val vec3: Vec3 = it.getPosition(sourceStack as CommandSourceStack)
      MCUtil.toPosition(vec3)
    }
  }

  override fun columnFinePosition(centerIntegers: Boolean): ArgumentType<ColumnFinePositionResolver> = wrap(
    Vec2Argument.vec2(centerIntegers),
  ) {
    ColumnFinePositionResolver { sourceStack ->
      val vec2: Vec3 = it.getPosition(sourceStack as CommandSourceStack)
      Impl.ColumnFinePositionImpl(vec2.x, vec2.y)
    }
  }

  override fun rotation(): ArgumentType<RotationResolver> = wrap(
    RotationArgument.rotation(),
  ) {
    RotationResolver { sourceStack ->
      val vec2: Vec2 = it.getRotation(sourceStack as CommandSourceStack)
      Rotation.rotation(vec2.y, vec2.x)
    }
  }

  override fun angle(): ArgumentType<AngleResolver> = wrap(
    AngleArgument.angle(),
  ) {
    AngleResolver { sourceStack ->
      it.getAngle(sourceStack as CommandSourceStack)
    }
  }

  override fun axes(): ArgumentType<AxisSet> = wrap(
    SwizzleArgument.swizzle(),
  ) {
    val bukkitAxes = EnumSet.noneOf(Axis::class.java)
    for (nmsAxis in it) {
      bukkitAxes.add(Axis.valueOf(nmsAxis.name))
    }

    Impl.AxisSetImpl(bukkitAxes)
  }

  override fun blockInWorldPredicate(): ArgumentType<BlockInWorldPredicate> = wrap(
    BlockPredicateArgument.blockPredicate(PaperCommands.INSTANCE.buildContext),
  ) {
    BlockInWorldPredicate { block, loadChunk ->
      val blockInWorld = BlockInWorld(
        (block.world as CraftWorld).handle,
        CraftLocation.toBlockPosition(block.location),
        loadChunk
      )
      if (blockInWorld.state == null) {
        BlockInWorldPredicate.Result.UNLOADED_CHUNK
      } else {
        if (it.test(blockInWorld)) BlockInWorldPredicate.Result.TRUE else BlockInWorldPredicate.Result.FALSE
      }
    }
  }

  override fun hexColor(): ArgumentType<TextColor> = wrap(
    HexColorArgument.hexColor()
  ) { TextColor.color(it) }

  private class HexColorArgument : ArgumentType<Int> {
    companion object {
      val EXAMPLES: Collection<String> = listOf("F00", "FF0000")
      val ERROR_INVALID_HEX: DynamicCommandExceptionType = DynamicCommandExceptionType {
        Component.translatableEscape(
          "argument.hexcolor.invalid",
          it
        )
      }

      fun color(alpha: Int, red: Int, green: Int, blue: Int): Int =
        alpha shl 24 or (red shl 16) or (green shl 8) or blue

      fun color(red: Int, green: Int, blue: Int): Int = color(255, red, green, blue)

      fun hexColor(): HexColorArgument = HexColorArgument()
    }

    @Throws(CommandSyntaxException::class)
    override fun parse(reader: StringReader): Int {
      val unquotedString = reader.readUnquotedString()
      return when (unquotedString.length) {
        3 -> color(
          duplicateDigit(Integer.parseInt(unquotedString, 0, 1, 16)),
          duplicateDigit(Integer.parseInt(unquotedString, 1, 2, 16)),
          duplicateDigit(Integer.parseInt(unquotedString, 2, 3, 16))
        )

        6 -> color(
          Integer.parseInt(unquotedString, 0, 2, 16),
          Integer.parseInt(unquotedString, 2, 4, 16),
          Integer.parseInt(unquotedString, 4, 6, 16)
        )

        else -> throw ERROR_INVALID_HEX.createWithContext(reader, unquotedString)
      }
    }

    private fun duplicateDigit(digit: Int): Int = digit * 17

    override fun <S> listSuggestions(
      context: CommandContext<S?>?,
      builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = SharedSuggestionProvider.suggest(EXAMPLES, builder)

    override fun getExamples(): Collection<String> = EXAMPLES
  }

  private fun <B, C> wrap(type: ArgumentType<B>, converter: ResultConverter<B, C>): ArgumentType<C> =
    DevCoreArgumentType1(type, converter)

  override fun team(): TeamArgumentType {
    return wrap(
      TeamArgument.team()
    ) {
      Bukkit.getScoreboardManager().mainScoreboard.getTeam(it)
    }
  }

  override fun slot(): SlotArgumentType = wrap(
    SlotArgument.slot()
  ) {
    it
  }

  override fun slots(): SlotsArgumentType = wrap(
    SlotsArgument.slots()
  ) {
    Impl.SlotRangeImpl(it.serializedName, it.slots())

  }

  override fun objective(): ObjectiveArgumentType = wrap(
    ObjectiveArgument.objective()
  ) {
    Bukkit.getScoreboardManager().mainScoreboard.getObjective(it)
  }

  internal class DevCoreArgumentType1<B, C>(argType: ArgumentType<B>, converter: ResultConverter<B, C>) :
    DevCoreArgumentType<B, C>(argType, converter) {
    override fun <S : Any> parse(reader: StringReader, source: S): C = converter.convert(argType.parse(reader, source))
  }
}

