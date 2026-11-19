package com.peco2282.devcore.teleport

import java.util.concurrent.CompletableFuture
import kotlin.math.max
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player

private val hazardous = setOf(
  Material.LAVA,
  Material.FIRE,
  Material.SOUL_FIRE,
  Material.CACTUS,
  Material.MAGMA_BLOCK,
  Material.CAMPFIRE,
  Material.SOUL_CAMPFIRE,
  Material.SWEET_BERRY_BUSH,
  Material.POWDER_SNOW,
)

/** Returns whether this location is a safe pair of standing/head blocks above a solid floor. */
fun Location.isSafeTeleportDestination(): Boolean {
  val world = world ?: return false
  val feet = world.getBlockAt(blockX, blockY, blockZ)
  val head = world.getBlockAt(blockX, blockY + 1, blockZ)
  val floor = world.getBlockAt(blockX, blockY - 1, blockZ)
  return floor.type.isSolid && floor.type !in hazardous &&
    feet.isPassable && !feet.isLiquid && feet.type !in hazardous &&
    head.isPassable && !head.isLiquid && head.type !in hazardous
}

/**
 * Finds the nearest safe block position in a bounded horizontal/vertical search.
 *
 * Horizontal candidates are considered in increasing Chebyshev distance, with vertical offsets
 * nearest the requested Y first. Returned X/Z coordinates are centered in the block.
 */
fun Location.findSafe(radius: Int = 5, verticalRadius: Int = 8): Location? {
  require(radius >= 0) { "Radius must be non-negative" }
  require(verticalRadius >= 0) { "Vertical radius must be non-negative" }
  val world = world ?: return null
  val verticalOffsets = buildList {
    add(0)
    for (distance in 1..verticalRadius) {
      add(-distance)
      add(distance)
    }
  }
  for (ring in 0..radius) {
    for (xOffset in -ring..ring) for (zOffset in -ring..ring) {
      if (max(kotlin.math.abs(xOffset), kotlin.math.abs(zOffset)) != ring) continue
      for (yOffset in verticalOffsets) {
        val y = blockY + yOffset
        if (y <= world.minHeight || y >= world.maxHeight - 1) continue
        val candidate = Location(
          world,
          blockX + xOffset + 0.5,
          y.toDouble(),
          blockZ + zOffset + 0.5,
          yaw,
          pitch,
        )
        if (candidate.isSafeTeleportDestination()) return candidate
      }
    }
  }
  return null
}

/** Loads every potentially searched chunk, finds a safe destination, then uses Paper async teleport. */
fun Player.teleportSafely(
  target: Location,
  radius: Int = 5,
  verticalRadius: Int = 8,
): CompletableFuture<Boolean> {
  require(radius >= 0) { "Radius must be non-negative" }
  val world = target.world ?: return CompletableFuture.completedFuture(false)
  val minChunkX = (target.blockX - radius) shr 4
  val maxChunkX = (target.blockX + radius) shr 4
  val minChunkZ = (target.blockZ - radius) shr 4
  val maxChunkZ = (target.blockZ + radius) shr 4
  val chunks = buildList {
    for (x in minChunkX..maxChunkX) for (z in minChunkZ..maxChunkZ) add(world.getChunkAtAsync(x, z))
  }
  return CompletableFuture.allOf(*chunks.toTypedArray()).thenCompose {
    val safe = target.findSafe(radius, verticalRadius)
      ?: return@thenCompose CompletableFuture.completedFuture(false)
    teleportAsync(safe)
  }
}
