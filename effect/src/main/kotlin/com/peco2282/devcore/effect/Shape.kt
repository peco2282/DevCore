package com.peco2282.devcore.effect

import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * A set of points expressed as offsets from an origin.
 *
 * Shapes are pure geometry: they know nothing about the server, so they can be
 * composed, transformed and unit-tested without one. Use them with [EffectBuilder.particle].
 */
fun interface Shape {
  /** Offsets from the origin. Each call returns fresh vectors that the caller may mutate. */
  fun points(): List<Vector>
}

private val UP = Vector(0.0, 1.0, 0.0)

/** A single point at the origin. */
fun point(): Shape = Shape { listOf(Vector(0.0, 0.0, 0.0)) }

/**
 * A straight line from the origin to [to].
 *
 * @param density Distance between neighbouring points, in blocks. Smaller is denser.
 */
fun line(to: Vector, density: Double = 0.25): Shape {
  require(density > 0.0) { "density must be positive: $density" }
  return Shape {
    val count = max(2, ceil(to.length() / density).toInt() + 1)
    List(count) { i -> to.clone().multiply(i.toDouble() / (count - 1)) }
  }
}

/**
 * A circle (or an arc) around the origin.
 *
 * @param radius Radius in blocks.
 * @param points Number of points around the full circle. With a partial [arc] only the
 *   proportional number of points is generated, so density stays constant.
 * @param normal Normal of the plane the circle lies in. Defaults to up, i.e. a flat circle.
 * @param arc Angle covered, in radians. Defaults to the full circle.
 * @param start Angle to start at, in radians.
 */
fun circle(
  radius: Double,
  points: Int = 36,
  normal: Vector = UP,
  arc: Double = 2 * PI,
  start: Double = 0.0
): Shape {
  require(points > 0) { "points must be positive: $points" }
  require(normal.lengthSquared() > 0.0) { "normal must not be the zero vector" }
  return Shape {
    val (u, v) = basis(normal)
    val full = abs(arc) >= 2 * PI - 1e-9
    val count = if (full) points else max(2, (points * abs(arc) / (2 * PI)).toInt() + 1)
    List(count) { i ->
      val t = start + arc * i / (if (full) count else count - 1)
      u.clone().multiply(cos(t) * radius).add(v.clone().multiply(sin(t) * radius))
    }
  }
}

/** A hollow sphere distributed evenly (Fibonacci lattice) around the origin. */
fun sphere(radius: Double, points: Int = 100): Shape {
  require(points > 0) { "points must be positive: $points" }
  return Shape {
    val golden = PI * (3 - sqrt(5.0))
    List(points) { i ->
      val y = if (points == 1) 0.0 else 1 - 2.0 * i / (points - 1)
      val r = sqrt(max(0.0, 1 - y * y))
      val theta = golden * i
      Vector(cos(theta) * r * radius, y * radius, sin(theta) * r * radius)
    }
  }
}

/**
 * A helix rising along +Y from the origin.
 *
 * @param height Total rise in blocks.
 * @param turns Number of full rotations over [height].
 */
fun helix(radius: Double, height: Double, turns: Double = 3.0, points: Int = 60): Shape {
  require(points > 1) { "points must be greater than 1: $points" }
  return Shape {
    List(points) { i ->
      val t = i.toDouble() / (points - 1)
      val a = 2 * PI * turns * t
      Vector(cos(a) * radius, height * t, sin(a) * radius)
    }
  }
}

/** Rotates every point around the X axis by [radians]. */
fun Shape.rotateX(radians: Double): Shape = Shape { points().onEach { it.rotateAroundX(radians) } }

/** Rotates every point around the Y axis by [radians]. */
fun Shape.rotateY(radians: Double): Shape = Shape { points().onEach { it.rotateAroundY(radians) } }

/** Rotates every point around the Z axis by [radians]. */
fun Shape.rotateZ(radians: Double): Shape = Shape { points().onEach { it.rotateAroundZ(radians) } }

/** Moves every point by [offset]. */
fun Shape.offset(offset: Vector): Shape = Shape { points().onEach { it.add(offset) } }

/** Moves every point by the given amounts. */
fun Shape.offset(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0): Shape = offset(Vector(x, y, z))

/** Scales every point away from the origin by [factor]. */
fun Shape.scale(factor: Double): Shape = Shape { points().onEach { it.multiply(factor) } }

/** Combines two shapes into one. */
operator fun Shape.plus(other: Shape): Shape = Shape { points() + other.points() }

/** Two perpendicular unit vectors spanning the plane with the given [normal]. */
private fun basis(normal: Vector): Pair<Vector, Vector> {
  val n = normal.clone().normalize()
  // Pick the axis least aligned with n to avoid a degenerate cross product.
  val helper = if (abs(n.y) < 0.9) UP else Vector(1.0, 0.0, 0.0)
  val u = n.clone().crossProduct(helper).normalize()
  val v = n.clone().crossProduct(u).normalize()
  return u to v
}

