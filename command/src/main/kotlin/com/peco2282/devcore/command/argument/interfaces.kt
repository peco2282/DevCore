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

/**
 * An [ArgumentResolver] that resolves a [ColumnBlockPosition] from a command argument.
 *
 * Use this type when retrieving a 2D integer block coordinate (X, Z) from a command context.
 */
typealias ColumnBlockPositionResolver  = ArgumentResolver<ColumnBlockPosition>

/**
 * An [ArgumentResolver] that resolves a [FinePosition] (precise 3D coordinates) from a command argument.
 */
typealias FinePositionResolver = ArgumentResolver<FinePosition>

/**
 * An [ArgumentResolver] that resolves a [ColumnFinePosition] (precise 2D coordinates) from a command argument.
 */
typealias ColumnFinePositionResolver = ArgumentResolver<ColumnFinePosition>

/**
 * An [ArgumentResolver] that resolves a [Rotation] (yaw and pitch) from a command argument.
 */
typealias RotationResolver = ArgumentResolver<Rotation>

/**
 * An [ArgumentResolver] that resolves a single angle ([Float]) from a command argument.
 */
typealias AngleResolver = ArgumentResolver<Float>

/**
 * A set of [Axis] values representing one or more coordinate axes (X, Y, Z).
 */
typealias AxisSet = Set<Axis>


/**
 * Represents a 2D block position in a Minecraft world, defined by integer X and Z coordinates.
 *
 * This is used as the result type for column block position arguments, where the Y coordinate
 * is not specified and must be provided separately.
 *
 * Example usage in a command:
 * ```kotlin
 * columnBlockPosition("pos") {
 *   executes { context ->
 *     val resolver = context.getArgument("pos", ColumnBlockPositionResolver::class.java)
 *     val col = resolver.resolve(context.source)
 *     val pos = col.toPosition(64) // supply Y coordinate
 *     1
 *   }
 * }
 * ```
 */
interface ColumnBlockPosition {
  /** The X coordinate of the column block position. */
  val blockX: Int

  /** The Z coordinate of the column block position. */
  val blockZ: Int

  /**
   * Converts this column position to a full [BlockPosition] by supplying a Y coordinate.
   *
   * @param y the Y coordinate to use
   * @return a [BlockPosition] with this column's X and Z and the given Y
   */
  fun toPosition(y: Int): BlockPosition = Position.block(this.blockX, y, this.blockZ)
}

/**
 * Represents a 2D fine (decimal) position in a Minecraft world, defined by double X and Z coordinates.
 *
 * This is used as the result type for column fine position arguments, where the Y coordinate
 * is not specified and must be provided separately.
 *
 * Example usage in a command:
 * ```kotlin
 * columnFinePosition("pos", centerIntegers = false) {
 *   executes { context ->
 *     val resolver = context.getArgument("pos", ColumnFinePositionResolver::class.java)
 *     val col = resolver.resolve(context.source)
 *     val pos = col.toPosition(64.0) // supply Y coordinate
 *     1
 *   }
 * }
 * ```
 */
interface ColumnFinePosition {
  /** The precise X coordinate of the column fine position. */
  val x: Double

  /** The precise Z coordinate of the column fine position. */
  val z: Double

  /**
   * Converts this column position to a full [FinePosition] by supplying a Y coordinate.
   *
   * @param y the Y coordinate to use
   * @return a [FinePosition] with this column's X and Z and the given Y
   */
  fun toPosition(y: Double): FinePosition {
    return Position.fine(this.x, y, this.z)
  }
}

/**
 * Represents a rotation defined by yaw and pitch angles (in degrees).
 *
 * This is used as the result type for rotation arguments in commands.
 *
 * Example usage in a command:
 * ```kotlin
 * rotation("rot") {
 *   executes { context ->
 *     val resolver = context.getArgument("rot", RotationResolver::class.java)
 *     val rot = resolver.resolve(context.source)
 *     println("Yaw: ${rot.yaw}, Pitch: ${rot.pitch}")
 *     1
 *   }
 * }
 * ```
 */
interface Rotation {
  companion object {
    /**
     * Creates a [Rotation] instance with the given yaw and pitch.
     *
     * This is an internal factory method used by DevCore implementations.
     *
     * @param yaw the yaw angle in degrees
     * @param pitch the pitch angle in degrees
     * @return a new [Rotation] instance
     */
    @DevCoreInternal
    fun rotation(yaw: Float, pitch: Float): Rotation = Impl.RotationImpl(yaw, pitch)
  }

  /** The pitch angle in degrees (vertical rotation, -90 to 90). */
  val pitch: Float

  /** The yaw angle in degrees (horizontal rotation, -180 to 180). */
  val yaw: Float
}


/**
 * A predicate that tests whether a block in the world matches certain conditions.
 *
 * Implementations of this interface are used as the result type for block predicate arguments.
 * The predicate can optionally load the chunk containing the block before testing.
 *
 * Example usage in a command:
 * ```kotlin
 * blockInWorldPredicate("filter") {
 *   executes { context ->
 *     val predicate = context.getArgument("filter", BlockInWorldPredicate::class.java)
 *     val block = player.location.block
 *     val result = predicate.testBlock(block)
 *     if (result.asBoolean()) player.sendMessage("Block matches!")
 *     1
 *   }
 * }
 * ```
 */
@ApiStatus.NonExtendable
fun interface BlockInWorldPredicate {
  /**
   * Checks if the passed block matches the block predicate, loading the chunk if necessary.
   *
   * @param block the block instance to check
   * @return the predicate result
   */
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


    /**
     * Converts this result to a [Boolean].
     *
     * @return `true` if this result is [TRUE], `false` otherwise
     */
    fun asBoolean(): Boolean = this == TRUE
  }
}

/**
 * An [ArgumentType] that resolves a scoreboard [Team] from a command argument.
 *
 * Use [DevCoreArgumentTypes.team] to obtain an instance.
 */
typealias TeamArgumentType = ArgumentType<Team?>

/**
 * An [ArgumentType] that resolves a single inventory slot index ([Int]) from a command argument.
 *
 * Use [DevCoreArgumentTypes.slot] to obtain an instance.
 */
typealias SlotArgumentType = ArgumentType<Int>

/**
 * Represents a range of inventory slots, identified by a serialized name and a list of slot indices.
 *
 * This is used as the result type for slots arguments in commands.
 *
 * Example usage in a command:
 * ```kotlin
 * slots("range") {
 *   executes { context ->
 *     val range = context.getArgument("range", SlotRange::class.java)
 *     println("Slots: ${range.slots}, Name: ${range.serializedName}")
 *     1
 *   }
 * }
 * ```
 */
interface SlotRange {
  /** The list of slot indices included in this range. */
  val slots: IntList

  /** The serialized string name of this slot range (e.g. `"hotbar.0"`). */
  val serializedName: String
}

/**
 * An [ArgumentType] that resolves a [SlotRange] (multiple inventory slots) from a command argument.
 *
 * Use [DevCoreArgumentTypes.slots] to obtain an instance.
 */
typealias SlotsArgumentType = ArgumentType<SlotRange>

/**
 * An [ArgumentType] that resolves a scoreboard [Objective] from a command argument.
 *
 * Use [DevCoreArgumentTypes.objective] to obtain an instance.
 */
typealias ObjectiveArgumentType = ArgumentType<Objective?>

/**
 * A functional interface for converting a value of type [T] to type [R].
 *
 * This is used internally by [DevCoreArgumentType] to transform parsed argument values.
 *
 * @param T the input type
 * @param R the output type
 */
fun interface ResultConverter<T, R> {
  /**
   * Converts a value of type [T] to type [R].
   *
   * @param var1 the value to convert
   * @return the converted value
   * @throws CommandSyntaxException if the conversion fails due to invalid input
   */
  @Throws(CommandSyntaxException::class)
  fun convert(var1: T): R
}
