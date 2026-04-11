package com.peco2282.devcore.command.argument

import com.peco2282.devcore.util.DevCoreInternal
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import io.papermc.paper.math.Position
import org.bukkit.Axis
import org.bukkit.block.Block

fun interface ColumnBlockPositionResolver : ArgumentResolver<ColumnBlockPosition>
interface ColumnBlockPosition {
  val blockX: Int
  val blockZ: Int

  fun toPosition(y: Int): BlockPosition = Position.block(this.blockX, y, this.blockZ)
}

fun interface FinePositionResolver : ArgumentResolver<FinePosition>

fun interface ColumnFinePositionResolver : ArgumentResolver<ColumnFinePosition>
interface ColumnFinePosition {
  val x: Double
  val z: Double

  fun toPosition(y: Double): FinePosition {
    return Position.fine(this.x, y, this.z)
  }
}


fun interface RotationResolver : ArgumentResolver<Rotation>
interface Rotation {
  companion object {
    @DevCoreInternal
    fun rotation(yaw: Float, pitch: Float): Rotation = Impl.RotationImpl(yaw, pitch)
  }

  val pitch: Float
  val yaw: Float
}

fun interface AngleResolver {
  fun resolve(sourceStack: CommandSourceStack): Float
}

interface AxisSet : Set<Axis>

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

