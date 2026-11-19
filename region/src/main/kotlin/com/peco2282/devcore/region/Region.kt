package com.peco2282.devcore.region

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block

/** Integer block bounds covered by a [Region]. */
data class RegionBounds(
  val minX: Int,
  val minY: Int,
  val minZ: Int,
  val maxX: Int,
  val maxY: Int,
  val maxZ: Int,
)

/** A geometric area restricted to one world. */
interface Region {
  val world: World
  val bounds: RegionBounds
  val center: Location
  val volume: Double

  operator fun contains(location: Location): Boolean

  /** Lazily visits the blocks whose coordinates are inside this region. */
  fun blocks(): Sequence<Block> = sequence {
    for (x in bounds.minX..bounds.maxX) for (y in bounds.minY..bounds.maxY) for (z in bounds.minZ..bounds.maxZ) {
      val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
      if (location in this@Region) yield(world.getBlockAt(x, y, z))
    }
  }

  fun randomLocation(random: Random = Random.Default): Location
}

fun cuboid(world: World, first: Location, second: Location): Region {
  require(first.world == world && second.world == world) { "Cuboid locations must belong to the supplied world" }
  return CuboidRegion(world, first, second)
}

fun sphere(center: Location, radius: Double): Region {
  require(radius.isFinite() && radius >= 0.0) { "Radius must be finite and non-negative" }
  return SphereRegion(requireNotNull(center.world), center.clone(), radius)
}

fun cylinder(base: Location, radius: Double, height: Double): Region {
  require(radius.isFinite() && radius >= 0.0) { "Radius must be finite and non-negative" }
  require(height.isFinite() && height >= 0.0) { "Height must be finite and non-negative" }
  return CylinderRegion(requireNotNull(base.world), base.clone(), radius, height)
}

private class CuboidRegion(
  override val world: World,
  first: Location,
  second: Location,
) : Region {
  private val minX = minOf(first.x, second.x)
  private val minY = minOf(first.y, second.y)
  private val minZ = minOf(first.z, second.z)
  private val maxX = maxOf(first.x, second.x)
  private val maxY = maxOf(first.y, second.y)
  private val maxZ = maxOf(first.z, second.z)
  override val bounds = RegionBounds(floor(minX).toInt(), floor(minY).toInt(), floor(minZ).toInt(), floor(maxX).toInt(), floor(maxY).toInt(), floor(maxZ).toInt())
  override val center = Location(world, (minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2)
  override val volume = (bounds.maxX - bounds.minX + 1.0) * (bounds.maxY - bounds.minY + 1.0) * (bounds.maxZ - bounds.minZ + 1.0)
  override fun contains(location: Location) = location.world == world && location.x in minX..maxX && location.y in minY..maxY && location.z in minZ..maxZ
  override fun randomLocation(random: Random): Location {
    fun coordinate(min: Double, max: Double) = if (min == max) min else random.nextDouble(min, Math.nextUp(max))
    return Location(world, coordinate(minX, maxX), coordinate(minY, maxY), coordinate(minZ, maxZ))
  }
}

private class SphereRegion(override val world: World, override val center: Location, private val radius: Double) : Region {
  override val bounds = RegionBounds(floor(center.x - radius).toInt(), floor(center.y - radius).toInt(), floor(center.z - radius).toInt(), ceil(center.x + radius).toInt(), ceil(center.y + radius).toInt(), ceil(center.z + radius).toInt())
  override val volume = 4.0 / 3.0 * PI * radius * radius * radius
  override fun contains(location: Location) = location.world == world && location.distanceSquared(center) <= radius * radius
  override fun randomLocation(random: Random): Location {
    if (radius == 0.0) return center.clone()
    while (true) {
      val x = random.nextDouble(-radius, radius)
      val y = random.nextDouble(-radius, radius)
      val z = random.nextDouble(-radius, radius)
      if (x * x + y * y + z * z <= radius * radius) return center.clone().add(x, y, z)
    }
  }
}

private class CylinderRegion(override val world: World, private val base: Location, private val radius: Double, private val height: Double) : Region {
  override val bounds = RegionBounds(floor(base.x - radius).toInt(), floor(base.y).toInt(), floor(base.z - radius).toInt(), ceil(base.x + radius).toInt(), ceil(base.y + height).toInt(), ceil(base.z + radius).toInt())
  override val center = base.clone().add(0.0, height / 2, 0.0)
  override val volume = PI * radius * radius * height
  override fun contains(location: Location): Boolean {
    if (location.world != world || location.y !in base.y..(base.y + height)) return false
    val x = location.x - base.x
    val z = location.z - base.z
    return x * x + z * z <= radius * radius
  }
  override fun randomLocation(random: Random): Location {
    val angle = random.nextDouble(0.0, PI * 2)
    val distance = sqrt(random.nextDouble()) * radius
    return Location(world, base.x + kotlin.math.cos(angle) * distance, base.y + random.nextDouble() * height, base.z + kotlin.math.sin(angle) * distance)
  }
}
