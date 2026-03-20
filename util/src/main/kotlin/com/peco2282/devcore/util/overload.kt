package com.peco2282.devcore.util

import org.bukkit.util.Vector


operator fun Vector.plus(other: Vector) = Vector(x + other.x, y + other.y, z + other.z)
operator fun Vector.minus(other: Vector) = Vector(x - other.x, y - other.y, z - other.z)
operator fun Vector.times(other: Vector) = Vector(x * other.x, y * other.y, z * other.z)
operator fun Vector.div(other: Vector) = Vector(x / other.x, y / other.y, z / other.z)

operator fun Vector.plus(other: Double) = Vector(x + other, y + other, z + other)
operator fun Vector.minus(other: Double) = Vector(x - other, y - other, z - other)
operator fun Vector.times(other: Double) = Vector(x * other, y * other, z * other)
operator fun Vector.div(other: Double) = Vector(x / other, y / other, z / other)
operator fun Vector.unaryMinus() = this - 1.0
operator fun Vector.unaryPlus() = this + 1.0

operator fun Vector.rem(other: Double) = Vector(x % other, y % other, z % other)
operator fun Vector.compareTo(other: Vector) =
  x.compareTo(other.x).takeIf { it == 0 } ?: y.compareTo(other.y).takeIf { it == 0 } ?: z.compareTo(other.z)
