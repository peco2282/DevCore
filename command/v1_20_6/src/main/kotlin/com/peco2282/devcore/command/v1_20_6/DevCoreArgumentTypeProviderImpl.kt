package com.peco2282.devcore.command.v1_20_6

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.peco2282.devcore.command.argument.*
import io.papermc.paper.command.brigadier.PaperCommands
import io.papermc.paper.util.MCUtil
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.*
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument
import net.minecraft.commands.arguments.coordinates.RotationArgument
import net.minecraft.commands.arguments.coordinates.SwizzleArgument
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.world.level.block.state.pattern.BlockInWorld
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.util.CraftLocation
import java.util.*

@Suppress("UnstableApiUsage")
class DevCoreArgumentTypeProviderImpl : DevCoreArgumentTypeProvider {
  companion object {
    val TYPE = Dynamic2CommandExceptionType { key, value ->
      LiteralMessage("Unknown $key : `$value`")
    }
  }

  override fun columnBlockPosition(): ArgumentType<ColumnBlockPositionResolver> =
    wrap(ColumnPosArgument.columnPos(), "Column Block Pos") {
      ColumnBlockPositionResolver { sourceStack ->
        val pos = it.getBlockPos(sourceStack as CommandSourceStack)
        Impl.ColumnBlockPositionImpl(pos.x, pos.z)
      }
    }

  override fun finePosition(centerIntegers: Boolean): ArgumentType<FinePositionResolver> = wrap(
    ColumnPosArgument.columnPos(),
    "Fine Position"
  ) {
    FinePositionResolver { sourceStack ->
      val pos = it.getPosition(sourceStack as CommandSourceStack)
      MCUtil.toPosition(pos)
    }
  }

  override fun columnFinePosition(centerIntegers: Boolean): ArgumentType<ColumnFinePositionResolver> = wrap(
    Vec2Argument.vec2(centerIntegers),
    "Column Fine Position"
  ) {
    ColumnFinePositionResolver { sourceStack ->
      val vec2: Vec3 = it.getPosition(sourceStack as CommandSourceStack)
      Impl.ColumnFinePositionImpl(vec2.x, vec2.y)
    }
  }

  override fun rotation(): ArgumentType<RotationResolver> = wrap(
    RotationArgument.rotation(),
    "Rotation"
  ) {
    RotationResolver { sourceStack ->
      val vec2: Vec2 = it.getRotation(sourceStack as CommandSourceStack)
      Rotation.rotation(vec2.y, vec2.x)
    }
  }

  override fun angle(): ArgumentType<AngleResolver> = wrap(
    AngleArgument.angle(),
    "Angle"
  ) {
    AngleResolver { sourceStack ->
      it.getAngle(sourceStack as CommandSourceStack)
    }
  }

  override fun axes(): ArgumentType<AxisSet> = wrap(
    SwizzleArgument.swizzle(),
    "Axes"
  ) {
    val bukkitAxes = EnumSet.noneOf(Axis::class.java)
    for (nmsAxis in it) {
      bukkitAxes.add(Axis.valueOf(nmsAxis.name))
    }

    Impl.AxisSetImpl(bukkitAxes)
  }

  override fun blockInWorldPredicate(): ArgumentType<BlockInWorldPredicate> = wrap(
    BlockPredicateArgument.blockPredicate(PaperCommands.INSTANCE.buildContext),
    "Block In World Predicate"
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

  private fun <T : Any, R : Any> wrap(
    argumentType: ArgumentType<T>,
    key: String,
    result: (T) -> R?
  ): ArgumentType<R> = NativeWrapperFactory.wrap(argumentType) {
    result(it) ?: throw TYPE.create(key, it)
  }


  override fun team(): TeamArgumentType {
    return wrap(
      TeamArgument.team(),
      "Team"
    ) {
      Bukkit.getScoreboardManager().mainScoreboard.getTeam(it)
    }
  }

  override fun slot(): SlotArgumentType = wrap(
    SlotArgument.slot(),
    "Slot"
  ) {
    it
  }

  override fun slots(): SlotsArgumentType = wrap(
    SlotsArgument.slots(),
    "Slots"
  ) {
    Impl.SlotRangeImpl(it.serializedName, it.slots())
  }

  override fun objective(): ObjectiveArgumentType = wrap(
    ObjectiveArgument.objective(),
    "Objective"
  ) {
    Bukkit.getScoreboardManager().mainScoreboard.getObjective(it)
  }

  override fun material(): MaterialArgumentType = wrap(
    ItemArgument.item(PaperCommands.INSTANCE.buildContext),
    "Material"
  ) {
    val key = NamespacedKey.fromString(it.item.toString())
      ?: NamespacedKey.minecraft(it.item.toString())
    Material.matchMaterial(key.toString())
  }

  override fun advancement(): AdvancementArgumentType = wrap(
    ResourceLocationArgument.id(),
    "Advancement"
  ) {
    Bukkit.getAdvancement(NamespacedKey.fromString(it.toString())!!)
  }

  override fun lootTable(): LootTableArgumentType = wrap(
    ResourceLocationArgument.id(),
    "Loot Table"
  ) {
    Bukkit.getLootTable(NamespacedKey.fromString(it.toString())!!)
  }
}