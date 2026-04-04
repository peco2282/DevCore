package com.peco2282.devcore.packet

import com.peco2282.devcore.packet.environment.EnvironmentHub
import com.peco2282.devcore.packet.interact.InteractHub
import com.peco2282.devcore.packet.vfx.VfxHub
import com.peco2282.devcore.packet.view.ViewHub
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * Central interface for all packet-level operations.
 *
 * Combines environment, interaction, visual effects, and view sub-hubs,
 * and exposes low-level packet utilities such as player injection, raw packet
 * sending, and NMS-backed builders.
 */
interface PacketHub : EnvironmentHub, InteractHub, VfxHub, ViewHub {
  /** Injects the Netty packet handler into the player's pipeline. */
  fun injectPlayer(player: Player)

  /** Removes the injected Netty packet handler from the player's pipeline. */
  fun removePlayer(player: Player)

  /**
   * Sends a raw NMS packet to the given player.
   *
   * @param player The target player.
   * @param packet The NMS packet instance (must be a `net.minecraft.network.protocol.Packet`).
   */
  fun sendPacket(player: Player, packet: Any)

  /**
   * Returns the [NetworkSettings] for the given player, allowing latency and packet-loss simulation.
   */
  fun getNetworkSettings(player: Player): NetworkSettings

  /**
   * Creates a [FakeEntityBuilder] for spawning a client-side fake entity.
   *
   * @param player The player who will see the fake entity.
   * @param type The entity type to spawn.
   * @param location The initial location of the fake entity.
   */
  fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder

  /**
   * Sends a plugin-channel (custom payload) packet to the player.
   *
   * @param player The target player.
   * @param channel The plugin channel identifier.
   * @param buf The raw byte buffer payload.
   */
  fun sendRawPacket(player: Player, channel: String, buf: ByteBuf)

  /**
   * Returns a [CoroutineDispatcher] bound to the player's Netty event loop,
   * or `null` if unavailable.
   */
  fun getCoroutineDispatcher(player: Player): CoroutineDispatcher?

  /**
   * Sends a title and subtitle to the player via packets.
   *
   * @param player The target player.
   * @param title The main title text.
   * @param subtitle The subtitle text.
   * @param fadeIn Fade-in duration in ticks.
   * @param stay Display duration in ticks.
   * @param fadeOut Fade-out duration in ticks.
   */
  fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

  /**
   * Sends an action bar message to the player via a packet.
   *
   * @param player The target player.
   * @param message The action bar text.
   */
  fun sendActionBar(player: Player, message: String)

  /**
   * Sends a named sound effect to the player via a packet.
   *
   * @param player The target player.
   * @param type The sound to play.
   * @param volume The volume level.
   * @param pitch The pitch level.
   * @param relative Whether the sound position is relative to the player.
   * @param offset The positional offset of the sound.
   */
  fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  )

  /**
   * Sends a particle effect to the player via a packet.
   *
   * @param player The target player.
   * @param type The particle type.
   * @param location The location to spawn particles at.
   * @param amount The number of particles.
   * @param offset The spread offset of the particles.
   * @param extra Extra data (e.g. speed).
   * @param data Optional particle data (e.g. `DustOptions`).
   */
  fun sendParticles(
    player: Player,
    type: Particle,
    location: Location,
    amount: Int,
    offset: Vector,
    extra: Double,
    data: Any?
  )

  /**
   * Sends fake block changes to the player using a [FakeBlockBuilder].
   *
   * @param player The target player.
   * @param builder DSL block for specifying fake block positions and materials.
   */
  fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit)

  /**
   * Sets the player's camera to the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity ID to attach the camera to.
   */
  fun sendCamera(player: Player, entityId: Int)

  /**
   * Sends a world border update packet to the player.
   *
   * @param player The target player.
   * @param builder DSL block for configuring the world border.
   */
  fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit)

  /**
   * Opens a sign editor for the player at the given location.
   *
   * @param player The target player.
   * @param location The location of the sign block.
   * @param front Whether to open the front face of the sign.
   */
  fun sendOpenSign(player: Player, location: Location, front: Boolean)

  /**
   * Sends entity metadata to the player.
   *
   * @param player The target player.
   * @param entityId The entity whose metadata is updated.
   * @param builder DSL block for specifying metadata entries.
   */
  fun sendMetadata(player: Player, entityId: Int, builder: MetadataBuilder.() -> Unit)

  // --- Entity Visibility ---

  /**
   * Hides the specified entity from the player by sending a remove-entities packet.
   *
   * @param player The target player.
   * @param entityId The entity ID to hide.
   */
  fun hideEntity(player: Player, entityId: Int)

  /**
   * Shows (re-spawns) the specified entity for the player.
   *
   * @param player The target player.
   * @param entityId The entity ID to show.
   */
  fun showEntity(player: Player, entityId: Int)

  // --- Equipment Faking ---

  /**
   * Sends a fake equipment packet to the player for the specified entity slot.
   *
   * @param player The target player.
   * @param entityId The entity whose equipment is faked.
   * @param slot The equipment slot to modify.
   * @param item The item to display in the slot.
   */
  override fun fakeEquipment(
    player: Player,
    entityId: Int,
    slot: EquipmentSlot,
    item: ItemStack
  )

  // --- Player Name Faking ---

  /**
   * Sends a fake player name (tab list / name tag) for [target] as seen by [player].
   *
   * Note: This is complex due to GameProfile handling.
   *
   * @param player The observing player.
   * @param target The player whose name is faked.
   * @param newName The fake display name.
   */
  fun fakePlayerName(player: Player, target: Player, newName: String)

  // --- Inventory ---

  /**
   * Updates the title of the player's currently open inventory via a packet.
   *
   * @param player The target player.
   * @param title The new inventory title.
   */
  fun updateInventoryTitle(player: Player, title: String)

  /**
   * Sends a fake item stack for a specific slot in the given window.
   *
   * @param player The target player.
   * @param windowId The container window ID.
   * @param slot The slot index.
   * @param item The item to display.
   */
  fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack)

  /**
   * Sends a fake furnace progress update to the player.
   *
   * @param player The target player.
   * @param windowId The furnace window ID.
   * @param progress The current progress value.
   * @param maxProgress The maximum progress value.
   */
  fun fakeFurnaceProgress(player: Player, windowId: Int, progress: Int, maxProgress: Int)

  // --- Weather / Environment ---

  /** @see EnvironmentHub.setFakeWeather */
  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean)

  /**
   * Sends a fake sky/fog color to the player (via biome hack or similar).
   *
   * @param player The target player.
   * @param color The ARGB color integer.
   */
  fun setFakeSkyColor(player: Player, color: Int)

  // --- Camera ---

  /**
   * Sets the player's camera to the specified entity ID.
   *
   * @param player The target player.
   * @param entityId The entity ID to use as the camera.
   */
  fun setCamera(player: Player, entityId: Int)

  // --- Animations ---

  /**
   * Sends an eating animation packet for the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity performing the animation.
   * @param eating Whether the entity is eating.
   * @param item The item being eaten, or `null`.
   */
  fun setEatingAnimation(player: Player, entityId: Int, eating: Boolean, item: ItemStack?)

  /**
   * Sends a bow-pulling animation packet for the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity performing the animation.
   * @param pulling Whether the entity is pulling the bow.
   */
  fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean)

  /**
   * Sends a shield/guard pose packet for the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param guarding Whether the entity is in guard pose.
   */
  fun setGuardPose(player: Player, entityId: Int, guarding: Boolean)

  /**
   * Sends a sleep animation packet for the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity to update.
   * @param sleeping Whether the entity is sleeping.
   * @param bedLocation The location of the bed, or `null`.
   */
  fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: Location?)

  // --- Motion ---

  /**
   * Sends a velocity/knockback packet for the specified entity.
   *
   * @param player The target player.
   * @param entityId The entity to apply motion to.
   * @param velocity The velocity vector.
   */
  fun setEntityMotion(player: Player, entityId: Int, velocity: Vector)

  // --- Statistics ---

  /**
   * Sends a fake statistic value to the player.
   *
   * @param player The target player.
   * @param category The statistic category name.
   * @param statistic The statistic name.
   * @param value The value to display.
   */
  fun fakeStatistic(player: Player, category: String, statistic: String, value: Int)

  // --- Experience ---

  /**
   * Sends a fake experience bar update to the player.
   *
   * @param player The target player.
   * @param level The experience level to display.
   * @param progress The progress within the current level (0.0–1.0).
   */
  fun fakeExperienceBar(player: Player, level: Int, progress: Float)

  // --- Item Cooldown ---

  /**
   * Sends an item cooldown packet to the player.
   *
   * @param player The target player.
   * @param material The material to apply the cooldown to.
   * @param ticks The cooldown duration in ticks.
   */
  fun setItemCooldown(player: Player, material: Material, ticks: Int)

  // --- Death Screen ---

  /**
   * Shows a fake death screen to the player with a custom message.
   *
   * @param player The target player.
   * @param message The death message to display.
   */
  fun showFakeDeathScreen(player: Player, message: String)
}
