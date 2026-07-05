package com.peco2282.devcore.util

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
