package com.peco2282.devcore.region

import java.util.UUID
import java.util.WeakHashMap
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin

/** Named region collection backed by a per-world chunk index. */
class RegionRegistry internal constructor() : Listener {
  private data class ChunkKey(val world: UUID, val x: Int, val z: Int)
  private val regions = linkedMapOf<String, Region>()
  private val index = mutableMapOf<ChunkKey, MutableSet<String>>()
  private val occupants = mutableMapOf<UUID, Set<String>>()
  private val enters = mutableMapOf<String, MutableList<(Player) -> Unit>>()
  private val leaves = mutableMapOf<String, MutableList<(Player) -> Unit>>()

  fun add(name: String, region: Region): Region? {
    require(name.isNotBlank()) { "Region name must not be blank" }
    val previous = remove(name)
    regions[name] = region
    chunkKeys(region).forEach { index.getOrPut(it) { linkedSetOf() } += name }
    return previous
  }

  fun remove(name: String): Region? {
    val removed = regions.remove(name) ?: return null
    chunkKeys(removed).forEach { key -> index[key]?.let { names -> names -= name; if (names.isEmpty()) index.remove(key) } }
    occupants.replaceAll { _, names -> names - name }
    enters.remove(name)
    leaves.remove(name)
    return removed
  }

  operator fun get(name: String): Region? = regions[name]

  fun at(location: Location): List<Region> {
    val world = location.world ?: return emptyList()
    val key = ChunkKey(world.uid, location.blockX shr 4, location.blockZ shr 4)
    return index[key].orEmpty().mapNotNull(regions::get).filter { location in it }
  }

  fun onEnter(name: String, action: (Player) -> Unit) { require(name in regions); enters.getOrPut(name) { mutableListOf() } += action }
  fun onLeave(name: String, action: (Player) -> Unit) { require(name in regions); leaves.getOrPut(name) { mutableListOf() } += action }

  /** Updates membership and invokes callbacks for the changed regions. */
  fun update(player: Player, location: Location = player.location) {
    val current = namesAt(location)
    val previous = occupants.put(player.uniqueId, current).orEmpty()
    (current - previous).forEach { name -> enters[name].orEmpty().forEach { it(player) } }
    (previous - current).forEach { name -> leaves[name].orEmpty().forEach { it(player) } }
  }

  @EventHandler
  fun onMove(event: PlayerMoveEvent) = updateIfBlockChanged(event.player, event.from, event.to)

  @EventHandler
  fun onTeleport(event: PlayerTeleportEvent) = updateIfBlockChanged(event.player, event.from, event.to)

  @EventHandler
  fun onQuit(event: PlayerQuitEvent) {
    val previous = occupants.remove(event.player.uniqueId).orEmpty()
    previous.forEach { name -> leaves[name].orEmpty().forEach { it(event.player) } }
  }

  private fun namesAt(location: Location): Set<String> {
    val world = location.world ?: return emptySet()
    val key = ChunkKey(world.uid, location.blockX shr 4, location.blockZ shr 4)
    return index[key].orEmpty().filterTo(linkedSetOf()) { name -> regions[name]?.contains(location) == true }
  }

  private fun updateIfBlockChanged(player: Player, from: Location, to: Location) {
    if (from.world == to.world && from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) return
    update(player, to)
  }

  private fun chunkKeys(region: Region): Sequence<ChunkKey> = sequence {
    val bounds = region.bounds
    for (x in (bounds.minX shr 4)..(bounds.maxX shr 4)) for (z in (bounds.minZ shr 4)..(bounds.maxZ shr 4)) {
      yield(ChunkKey(region.world.uid, x, z))
    }
  }

  companion object {
    private val registries = WeakHashMap<Plugin, RegionRegistry>()

    internal fun forPlugin(plugin: Plugin): RegionRegistry = synchronized(registries) {
      registries.getOrPut(plugin) {
        RegionRegistry().also { plugin.server.pluginManager.registerEvents(it, plugin) }
      }
    }
  }
}

/** Returns this plugin's lifecycle-bound region registry. */
fun Plugin.regions(): RegionRegistry = RegionRegistry.forPlugin(this)
