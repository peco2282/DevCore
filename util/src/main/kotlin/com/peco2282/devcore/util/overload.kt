package com.peco2282.devcore.util

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.util.Vector


/**
 * Gets the Block at this Location's block coordinates.
 *
 * This property retrieves the block at the integer block coordinates (blockX, blockY, blockZ)
 * of this Location in its world.
 *
 * Example:
 * ```kotlin
 * val location = Location(world, 10.5, 64.8, -20.3)
 * val block = location.block // Gets block at (10, 64, -20)
 * ```
 *
 * @return The Block at this Location's block coordinates
 */
val Location.block: Block get() = blockX.let { x -> blockY.let { y -> blockZ.let { z -> world.getBlockAt(x, y, z) } } }

/**
 * Creates a new Location with the specified offsets added to this Location's coordinates.
 *
 * This function clones the current Location and adds the specified x, y, and z offsets.
 * All parameters are optional and default to 0.0.
 *
 * Example:
 * ```kotlin
 * val original = Location(world, 10.0, 64.0, -20.0)
 * val offset = original.addOffset(x = 5.0, y = 10.0) // (15.0, 74.0, -20.0)
 * ```
 *
 * @param x The x-coordinate offset to add (default: 0.0)
 * @param y The y-coordinate offset to add (default: 0.0)
 * @param z The z-coordinate offset to add (default: 0.0)
 * @return A new Location with the offsets applied
 */
fun Location.addOffset(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Location = clone().add(x, y, z)

/**
 * Adds two Vectors component-wise.
 *
 * Example:
 * ```kotlin
 * val v1 = Vector(1.0, 2.0, 3.0)
 * val v2 = Vector(4.0, 5.0, 6.0)
 * val result = v1 + v2 // Vector(5.0, 7.0, 9.0)
 * ```
 *
 * @param other The Vector to add
 * @return A new Vector with component-wise sum
 */
operator fun Vector.plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)

/**
 * Subtracts two Vectors component-wise.
 *
 * Example:
 * ```kotlin
 * val v1 = Vector(10.0, 8.0, 6.0)
 * val v2 = Vector(4.0, 3.0, 2.0)
 * val result = v1 - v2 // Vector(6.0, 5.0, 4.0)
 * ```
 *
 * @param other The Vector to subtract
 * @return A new Vector with component-wise difference
 */
operator fun Vector.minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)

/**
 * Multiplies two Vectors component-wise.
 *
 * Example:
 * ```kotlin
 * val v1 = Vector(2.0, 3.0, 4.0)
 * val v2 = Vector(5.0, 6.0, 7.0)
 * val result = v1 * v2 // Vector(10.0, 18.0, 28.0)
 * ```
 *
 * @param other The Vector to multiply by
 * @return A new Vector with component-wise product
 */
operator fun Vector.times(other: Vector) = Vector(x * other.x, y * other.y, z * other.z)

/**
 * Divides two Vectors component-wise.
 *
 * Example:
 * ```kotlin
 * val v1 = Vector(10.0, 20.0, 30.0)
 * val v2 = Vector(2.0, 4.0, 5.0)
 * val result = v1 / v2 // Vector(5.0, 5.0, 6.0)
 * ```
 *
 * @param other The Vector to divide by
 * @return A new Vector with component-wise quotient
 */
operator fun Vector.div(other: Vector) = Vector(x / other.x, y / other.y, z / other.z)

/**
 * Adds a scalar value to all components of the Vector.
 *
 * Example:
 * ```kotlin
 * val v = Vector(1.0, 2.0, 3.0)
 * val result = v + 5.0 // Vector(6.0, 7.0, 8.0)
 * ```
 *
 * @param other The scalar value to add to each component
 * @return A new Vector with the scalar added to all components
 */
operator fun Vector.plus(other: Double) = Vector(x + other, y + other, z + other)

/**
 * Subtracts a scalar value from all components of the Vector.
 *
 * Example:
 * ```kotlin
 * val v = Vector(10.0, 8.0, 6.0)
 * val result = v - 2.0 // Vector(8.0, 6.0, 4.0)
 * ```
 *
 * @param other The scalar value to subtract from each component
 * @return A new Vector with the scalar subtracted from all components
 */
operator fun Vector.minus(other: Double) = Vector(x - other, y - other, z - other)

/**
 * Multiplies all components of the Vector by a scalar value.
 *
 * Example:
 * ```kotlin
 * val v = Vector(1.0, 2.0, 3.0)
 * val result = v * 3.0 // Vector(3.0, 6.0, 9.0)
 * ```
 *
 * @param other The scalar value to multiply each component by
 * @return A new Vector with all components scaled
 */
operator fun Vector.times(other: Double) = Vector(x * other, y * other, z * other)

/**
 * Divides all components of the Vector by a scalar value.
 *
 * Example:
 * ```kotlin
 * val v = Vector(10.0, 20.0, 30.0)
 * val result = v / 2.0 // Vector(5.0, 10.0, 15.0)
 * ```
 *
 * @param other The scalar value to divide each component by
 * @return A new Vector with all components divided
 */
operator fun Vector.div(other: Double) = Vector(x / other, y / other, z / other)

/**
 * Unary minus operator that subtracts 1.0 from all components.
 *
 * Example:
 * ```kotlin
 * val v = Vector(5.0, 3.0, 2.0)
 * val result = -v // Vector(4.0, 2.0, 1.0)
 * ```
 *
 * @return A new Vector with 1.0 subtracted from all components
 */
operator fun Vector.unaryMinus() = this - 1.0

/**
 * Unary plus operator that adds 1.0 to all components.
 *
 * Example:
 * ```kotlin
 * val v = Vector(5.0, 3.0, 2.0)
 * val result = +v // Vector(6.0, 4.0, 3.0)
 * ```
 *
 * @return A new Vector with 1.0 added to all components
 */
operator fun Vector.unaryPlus() = this + 1.0

/**
 * Calculates the modulo of all components with a scalar value.
 *
 * Example:
 * ```kotlin
 * val v = Vector(10.0, 15.0, 20.0)
 * val result = v % 7.0 // Vector(3.0, 1.0, 6.0)
 * ```
 *
 * @param other The scalar value to calculate modulo with
 * @return A new Vector with modulo applied to all components
 */
operator fun Vector.rem(other: Double) = Vector(x % other, y % other, z % other)

/**
 * Compares two Vectors lexicographically (x, then y, then z).
 *
 * Compares first by x-coordinate, then by y-coordinate if x values are equal,
 * and finally by z-coordinate if both x and y values are equal.
 *
 * Example:
 * ```kotlin
 * val v1 = Vector(1.0, 2.0, 3.0)
 * val v2 = Vector(1.0, 3.0, 2.0)
 * val comparison = v1 < v2 // true (because y1 < y2)
 * ```
 *
 * @param other The Vector to compare to
 * @return Negative if this < other, positive if this > other, zero if equal
 */
operator fun Vector.compareTo(other: Vector) =
  x.compareTo(other.x).takeIf { it == 0 } ?: y.compareTo(other.y).takeIf { it == 0 } ?: z.compareTo(other.z)


/**
 * Adds a Vector to this Location's coordinates.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 64.0, -20.0)
 * val vec = Vector(5.0, 10.0, 3.0)
 * val result = loc + vec // Location at (15.0, 74.0, -17.0)
 * ```
 *
 * @param vector The Vector to add
 * @return A new Location with the Vector added
 */
operator fun Location.plus(vector: Vector): Location = clone().add(vector)

/**
 * Subtracts a Vector from this Location's coordinates.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 64.0, -20.0)
 * val vec = Vector(5.0, 10.0, 3.0)
 * val result = loc - vec // Location at (5.0, 54.0, -23.0)
 * ```
 *
 * @param vector The Vector to subtract
 * @return A new Location with the Vector subtracted
 */
operator fun Location.minus(vector: Vector): Location = clone().subtract(vector)

/**
 * Multiplies this Location's coordinates component-wise with a Vector.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val vec = Vector(2.0, 3.0, 4.0)
 * val result = loc * vec // Location at (20.0, 60.0, 120.0)
 * ```
 *
 * @param vector The Vector to multiply by
 * @return A new Location with component-wise multiplication
 */
operator fun Location.times(vector: Vector): Location = Location(world, x * vector.x, y * vector.y, z * vector.z)

/**
 * Divides this Location's coordinates component-wise by a Vector.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val vec = Vector(2.0, 4.0, 5.0)
 * val result = loc / vec // Location at (5.0, 5.0, 6.0)
 * ```
 *
 * @param vector The Vector to divide by
 * @return A new Location with component-wise division
 */
operator fun Location.div(vector: Vector): Location = Location(world, x / vector.x, y / vector.y, z / vector.z)

/**
 * Adds another Location's coordinates to this Location.
 *
 * Example:
 * ```kotlin
 * val loc1 = Location(world, 10.0, 20.0, 30.0)
 * val loc2 = Location(world, 5.0, 10.0, 15.0)
 * val result = loc1 + loc2 // Location at (15.0, 30.0, 45.0)
 * ```
 *
 * @param other The Location whose coordinates to add
 * @return A new Location with the coordinates added
 */
operator fun Location.plus(other: Location): Location = clone().add(other)

/**
 * Subtracts another Location's coordinates from this Location.
 *
 * Example:
 * ```kotlin
 * val loc1 = Location(world, 10.0, 20.0, 30.0)
 * val loc2 = Location(world, 5.0, 10.0, 15.0)
 * val result = loc1 - loc2 // Location at (5.0, 10.0, 15.0)
 * ```
 *
 * @param other The Location whose coordinates to subtract
 * @return A new Location with the coordinates subtracted
 */
operator fun Location.minus(other: Location): Location = clone().subtract(other)

/**
 * Multiplies this Location's coordinates component-wise with another Location.
 *
 * Example:
 * ```kotlin
 * val loc1 = Location(world, 10.0, 20.0, 30.0)
 * val loc2 = Location(world, 2.0, 3.0, 4.0)
 * val result = loc1 * loc2 // Location at (20.0, 60.0, 120.0)
 * ```
 *
 * @param other The Location to multiply by
 * @return A new Location with component-wise multiplication
 */
operator fun Location.times(other: Location): Location = Location(world, x * other.x, y * other.y, z * other.z)

/**
 * Divides this Location's coordinates component-wise by another Location.
 *
 * Example:
 * ```kotlin
 * val loc1 = Location(world, 10.0, 20.0, 30.0)
 * val loc2 = Location(world, 2.0, 4.0, 5.0)
 * val result = loc1 / loc2 // Location at (5.0, 5.0, 6.0)
 * ```
 *
 * @param other The Location to divide by
 * @return A new Location with component-wise division
 */
operator fun Location.div(other: Location): Location = Location(world, x / other.x, y / other.y, z / other.z)

/**
 * Adds a scalar value to all coordinates of this Location.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val result = loc + 5.0 // Location at (15.0, 25.0, 35.0)
 * ```
 *
 * @param other The scalar value to add to all coordinates
 * @return A new Location with the scalar added to all coordinates
 */
operator fun Location.plus(other: Double): Location = clone().add(other, other, other)

/**
 * Subtracts a scalar value from all coordinates of this Location.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val result = loc - 5.0 // Location at (5.0, 15.0, 25.0)
 * ```
 *
 * @param other The scalar value to subtract from all coordinates
 * @return A new Location with the scalar subtracted from all coordinates
 */
operator fun Location.minus(other: Double): Location = clone().subtract(other, other, other)

/**
 * Multiplies all coordinates of this Location by a scalar value.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val result = loc * 2.0 // Location at (20.0, 40.0, 60.0)
 * ```
 *
 * @param other The scalar value to multiply all coordinates by
 * @return A new Location with all coordinates scaled
 */
operator fun Location.times(other: Double): Location = clone().multiply(other)

/**
 * Divides all coordinates of this Location by a scalar value.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 20.0, 30.0)
 * val result = loc / 2.0 // Location at (5.0, 10.0, 15.0)
 * ```
 *
 * @param other The scalar value to divide all coordinates by
 * @return A new Location with all coordinates divided
 */
operator fun Location.div(other: Double): Location = Location(world, x / other, y / other, z / other)

/**
 * Calculates the modulo of this Location's coordinates component-wise with another Location.
 *
 * Example:
 * ```kotlin
 * val loc1 = Location(world, 10.0, 15.0, 20.0)
 * val loc2 = Location(world, 7.0, 6.0, 8.0)
 * val result = loc1 % loc2 // Location at (3.0, 3.0, 4.0)
 * ```
 *
 * @param other The Location to calculate modulo with
 * @return A new Location with component-wise modulo
 */
operator fun Location.rem(other: Location): Location = Location(world, x % other.x, y % other.y, z % other.z)

/**
 * Creates a new Location with selectively modified properties.
 *
 * This function allows creating a copy of a Location with specific properties changed
 * while keeping others unchanged. All parameters default to the current Location's values.
 *
 * Example:
 * ```kotlin
 * val original = Location(world, 10.0, 64.0, -20.0, 90f, 0f)
 * val modified = original.with(y = 100.0, pitch = 45f) // Only y and pitch changed
 * ```
 *
 * @param world The world for the new Location (default: current world)
 * @param x The x-coordinate for the new Location (default: current x)
 * @param y The y-coordinate for the new Location (default: current y)
 * @param z The z-coordinate for the new Location (default: current z)
 * @param yaw The yaw rotation for the new Location (default: current yaw)
 * @param pitch The pitch rotation for the new Location (default: current pitch)
 * @return A new Location with the specified properties
 */
fun Location.with(
  world: World = this.world,
  x: Double = this.x,
  y: Double = this.y,
  z: Double = this.z,
  yaw: Float = this.yaw,
  pitch: Float = this.pitch
): Location = Location(world, x, y, z, yaw, pitch)

/**
 * Gets the center point of the block containing this Location.
 *
 * This property returns a new Location at the center of the block by adding 0.5
 * to each coordinate. Useful for centering entities or effects within a block.
 *
 * Example:
 * ```kotlin
 * val loc = Location(world, 10.0, 64.0, -20.0)
 * val centered = loc.center // Location at (10.5, 64.5, -19.5)
 * ```
 *
 * @return A new Location at the center of the block
 */
val Location.center: Location
  get() = clone().add(0.5, 0.5, 0.5)

/**
 * Creates a new Location with direction set to face towards a target Location.
 *
 * This function calculates the direction vector from this Location to the target
 * and returns a new Location with the yaw and pitch set to face that direction.
 *
 * Example:
 * ```kotlin
 * val origin = Location(world, 0.0, 64.0, 0.0)
 * val target = Location(world, 10.0, 64.0, 10.0)
 * val facing = origin.face(target) // Direction set to face (10, 64, 10)
 * ```
 *
 * @param target The Location to face towards
 * @return A new Location with direction set to face the target
 */
fun Location.face(target: Location): Location {
  val direction = target.toVector().subtract(this.toVector())
  return clone().setDirection(direction)
}
