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

interface PacketHub : EnvironmentHub, InteractHub, VfxHub, ViewHub {
  fun injectPlayer(player: Player)
  fun removePlayer(player: Player)
  fun sendPacket(player: Player, packet: Any)
  fun getNetworkSettings(player: Player): NetworkSettings
  fun createFakeEntityBuilder(player: Player, type: EntityType, location: Location): FakeEntityBuilder
  fun sendRawPacket(player: Player, channel: String, buf: ByteBuf)
  fun getCoroutineDispatcher(player: Player): CoroutineDispatcher?
  fun sendTitle(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)
  fun sendActionBar(player: Player, message: String)
  fun sendSound(
    player: Player,
    type: Sound,
    volume: Float,
    pitch: Float,
    relative: Boolean,
    offset: Vector
  )

  fun sendParticles(
    player: Player,
    type: Particle,
    location: Location,
    amount: Int,
    offset: Vector,
    extra: Double,
    data: Any?
  )

  fun sendFakeBlocks(player: Player, builder: FakeBlockBuilder.() -> Unit)

  fun sendCamera(player: Player, entityId: Int)
  fun sendWorldBorder(player: Player, builder: WorldBorderBuilder.() -> Unit)
  fun sendOpenSign(player: Player, location: Location, front: Boolean)
  fun sendMetadata(player: Player, entityId: Int, builder: MetadataBuilder.() -> Unit)

  // --- New Packet Actions ---

  // 1. Entity Visibility
  fun hideEntity(player: Player, entityId: Int)
  fun showEntity(player: Player, entityId: Int) // Re-send spawn packet if needed, or metadata

  // 2. Equipment Faking
  override fun fakeEquipment(
    player: Player,
    entityId: Int,
    slot: EquipmentSlot,
    item: ItemStack
  )

  // 3. Player Name Faking (Tab/Above head) - Note: This is complex due to GameProfile
  fun fakePlayerName(player: Player, target: Player, newName: String)

  // 4. Inventory Title change
  fun updateInventoryTitle(player: Player, title: String)

  // 5. Fake Item in Slot
  fun fakeItemSlot(player: Player, windowId: Int, slot: Int, item: ItemStack)

  // 6. Furnace Progress
  fun fakeFurnaceProgress(player: Player, windowId: Int, progress: Int, maxProgress: Int)

  // 8. Weather
  override fun setFakeWeather(player: Player, rain: Boolean, thunder: Boolean)
  // 9. Sky/Fog Color (via Biome hack or other means)
  fun setFakeSkyColor(player: Player, color: Int)

  // 10. Camera
  fun setCamera(player: Player, entityId: Int)

  // 11. Eating Animation
  fun setEatingAnimation(player: Player, entityId: Int, eating: Boolean, item: ItemStack?)

  // 12. Bow Animation
  fun setBowAnimation(player: Player, entityId: Int, pulling: Boolean)

  // 13. Shield/Guard Pose
  fun setGuardPose(player: Player, entityId: Int, guarding: Boolean)

  // 14. Sleep Animation
  fun setSleepAnimation(player: Player, entityId: Int, sleeping: Boolean, bedLocation: Location?)

  // 15. Motion/Knockback (Override)
  fun setEntityMotion(player: Player, entityId: Int, velocity: Vector)

  // 16. Statistics
  fun fakeStatistic(player: Player, category: String, statistic: String, value: Int)

  // 17. Experience Bar
  fun fakeExperienceBar(player: Player, level: Int, progress: Float)

  // 18. Sound Replacement is handled by PacketListener + Intercept

  // 19. Item Cooldown
  fun setItemCooldown(player: Player, material: Material, ticks: Int)

  // 20. Fake Death Screen
  fun showFakeDeathScreen(player: Player, message: String)

}