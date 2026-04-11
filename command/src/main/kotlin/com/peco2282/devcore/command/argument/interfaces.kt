@file:Suppress("NonExtendableApiUsage", "UnstableApiUsage")

package com.peco2282.devcore.command.argument

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.peco2282.devcore.util.DevCoreInternal
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import io.papermc.paper.math.Position
import it.unimi.dsi.fastutil.ints.IntList
import org.bukkit.Axis
import org.bukkit.block.Block
import org.bukkit.entity.Bee
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import org.jetbrains.annotations.ApiStatus

typealias ColumnBlockPositionResolver  = ArgumentResolver<ColumnBlockPosition>
typealias FinePositionResolver = ArgumentResolver<FinePosition>
typealias ColumnFinePositionResolver = ArgumentResolver<ColumnFinePosition>
typealias RotationResolver = ArgumentResolver<Rotation>
typealias AngleResolver = ArgumentResolver<Float>
typealias AxisSet = Set<Axis>


interface ColumnBlockPosition {
  val blockX: Int
  val blockZ: Int

  fun toPosition(y: Int): BlockPosition = Position.block(this.blockX, y, this.blockZ)
}

interface ColumnFinePosition {
  val x: Double
  val z: Double

  fun toPosition(y: Double): FinePosition {
    return Position.fine(this.x, y, this.z)
  }
}

interface Rotation {
  companion object {
    @DevCoreInternal
    fun rotation(yaw: Float, pitch: Float): Rotation = Impl.RotationImpl(yaw, pitch)
  }

  val pitch: Float
  val yaw: Float
}


@ApiStatus.NonExtendable
fun interface BlockInWorldPredicate {
  fun testBlock(block: Block): Result = this.testBlock(block, true)

  /**
   * Checks if the passed block matches the block predicate.
   *
   * @param block     the block instance to check
   * @param loadChunk if the chunk the block is located at should be loaded.
   * @return the predicate result.
   */
  fun testBlock(block: Block, loadChunk: Boolean): Result

  /**
   * The predicate result is yielded by the [BlockInWorldPredicate] when applied to a block.
   *
   * @see .testBlock
   * @see .testBlock
   */
  enum class Result {
    /**
     * The block passed to the predicate matches the predicate.
     */
    TRUE,

    /**
     * The block passed to the predicate does not match the predicate.
     */
    FALSE,

    /**
     * The block passed to the predicate was in an unloaded chunk and the `loadChunk` flag was false.
     */
    UNLOADED_CHUNK,
    ;


    fun asBoolean(): Boolean = this == TRUE
  }
}

// Minecraft
typealias TeamArgumentType = ArgumentType<Team?>
typealias SlotArgumentType = ArgumentType<Int>

interface SlotRange {
  val slots: IntList
  val serializedName: String
}

typealias SlotsArgumentType = ArgumentType<SlotRange>
typealias ObjectiveArgumentType = ArgumentType<Objective?>

fun interface ResultConverter<T, R> {
  @Throws(CommandSyntaxException::class)
  fun convert(var1: T): R
}
