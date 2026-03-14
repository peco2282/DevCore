package com.peco2282.devcore.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.player.Equipment
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * DSL marker for fake visual elements.
 */
@DslMarker
annotation class FakeVisualDsl

/**
 * Entry point for sending fake visuals to a player.
 *
 * Example usage:
 * ```kotlin
 * player.sendFakeVisuals {
 *     spawnEntity(EntityType.ZOMBIE, location) {
 *         customName = "Fake Boss"
 *         isGlowing = true
 *         equipment {
 *             helmet = ItemStack(Material.GOLDEN_HELMET)
 *         }
 *     }
 *     setFakeBlock(location.add(0.0, 1.0, 0.0), Material.DIAMOND_BLOCK)
 * }
 * ```
 */
inline fun Player.sendFakeVisuals(block: FakeVisualContext.() -> Unit) {
  FakeVisualContext(this).apply(block)
}

/**
 * Context for defining fake visuals for a specific player.
 */
class FakeVisualContext(val player: Player) {
  /**
   * Spawns a fake entity for the player.
   *
   * @param type The type of the entity to spawn.
   * @param loc The location to spawn the entity.
   * @param entityId The ID for the fake entity (optional, default is a random short-lived ID).
   * @param block The builder block for the entity.
   */
  inline fun spawnEntity(
    type: EntityType,
    loc: Location,
    entityId: Int = (System.currentTimeMillis() % 100000).toInt(),
    block: FakeEntityBuilder.() -> Unit
  ) {
    val builder = FakeEntityBuilder(type, entityId).apply(block)
    builder.send(player, loc)
  }

  /**
   * Sends a fake block change to the player.
   *
   * @param loc The location of the block.
   * @param material The material to show.
   */
  fun setFakeBlock(loc: Location, material: Material) {
    player.sendBlockChange(loc, material.createBlockData())
  }
}

/**
 * A builder for creating fake entity visuals using a DSL.
 *
 * @param type The type of the entity.
 * @param entityId The ID of the entity.
 */
@FakeVisualDsl
class FakeEntityBuilder(val type: EntityType, val entityId: Int) {
  /**
   * The custom name of the entity.
   */
  var customName: String? = null

  /**
   * Whether the entity has the glowing effect.
   */
  var isGlowing: Boolean = false

  /**
   * Whether the entity is invisible.
   */
  var isInvisible: Boolean = false

  private val equipment = mutableMapOf<EquipmentSlot, ItemStack>()

  /**
   * Configures the equipment of the fake entity.
   */
  fun equipment(block: EquipmentBuilder.() -> Unit) {
    equipment.putAll(EquipmentBuilder().apply(block).items)
  }

  /**
   * Builds and sends the necessary packets to the player.
   *
   * @param player The player to receive the packets.
   * @param loc The location of the fake entity.
   */
  fun send(player: Player, loc: Location) {
    val user = PacketEvents.getAPI().protocolManager.getUser(player) ?: return

    // 1. Spawn Packet
    val spawnPacket = WrapperPlayServerSpawnEntity(
      entityId,
      Optional.of(UUID.randomUUID()),
      type,
      Vector3d(loc.x, loc.y, loc.z),
      loc.pitch,
      loc.yaw,
      0f, 0, null
    )

    // 2. Metadata (Entity state)
    val metadata = mutableListOf<EntityData<*>>()

    // Basic status (Index 0)
    var bitmask = 0x00.toByte()
    if (isInvisible) bitmask = (bitmask.toInt() or 0x20).toByte()
    if (isGlowing) bitmask = (bitmask.toInt() or 0x40).toByte()
    metadata.add(EntityData(0, EntityDataTypes.BYTE, bitmask))

    // Custom Name
    customName?.let {
      metadata.add(EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(it)))
      metadata.add(EntityData(3, EntityDataTypes.BOOLEAN, true)) // Always show name
    }

    val metadataPacket = WrapperPlayServerEntityMetadata(entityId, metadata)

    // Send packets
    user.sendPacket(spawnPacket)
    user.sendPacket(metadataPacket)

    // 3. Equipment Packet
    if (equipment.isNotEmpty()) {
      val items = equipment.map { (slot, item) ->
        Equipment(
          slot,
          SpigotConversionUtil.fromBukkitItemStack(item)
        )
      }
      user.sendPacket(WrapperPlayServerEntityEquipment(entityId, items))
    }
  }
}
