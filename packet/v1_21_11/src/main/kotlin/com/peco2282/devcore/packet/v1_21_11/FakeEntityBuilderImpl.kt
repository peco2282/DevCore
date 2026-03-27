package com.peco2282.devcore.packet.v1_21_11

import com.peco2282.devcore.packet.EntityAnimation
import com.peco2282.devcore.packet.EquipmentBuilder
import com.peco2282.devcore.packet.FakeEntityBuilder
import com.peco2282.devcore.scheduler.PluginRegistory
import com.peco2282.devcore.scheduler.ticks
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundAnimatePacket
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.item.ItemStack as NMSItemStack
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import net.minecraft.world.entity.EquipmentSlot as NMSEquipmentSlot
import com.mojang.datafixers.util.Pair as DataPair
import com.peco2282.devcore.packet.setFieldValue
import org.bukkit.Bukkit

class FakeEntityBuilderImpl(
  private val player: Player,
  private val type: EntityType,
  private val location: Location
) : FakeEntityBuilder {

  private val entityId = (Math.random() * Int.MAX_VALUE).toInt()
  private val uuid = UUID.randomUUID()
  
  override var customName: String? = null
  override var isCustomNameVisible: Boolean = false
  override var isInvisible: Boolean = false
  override var isGlowing: Boolean = false

  private var equipment: List<DataPair<NMSEquipmentSlot, NMSItemStack>> = emptyList()

  override fun equipment(builder: EquipmentBuilder.() -> Unit) {
    val equipmentBuilder = EquipmentBuilder().apply(builder)
    val list = mutableListOf<DataPair<NMSEquipmentSlot, NMSItemStack>>()
    
    equipmentBuilder.mainHand?.let { list.add(DataPair(NMSEquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(it))) }
    equipmentBuilder.offHand?.let { list.add(DataPair(NMSEquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(it))) }
    equipmentBuilder.helmet?.let { list.add(DataPair(NMSEquipmentSlot.HEAD, CraftItemStack.asNMSCopy(it))) }
    equipmentBuilder.chestplate?.let { list.add(DataPair(NMSEquipmentSlot.CHEST, CraftItemStack.asNMSCopy(it))) }
    equipmentBuilder.leggings?.let { list.add(DataPair(NMSEquipmentSlot.LEGS, CraftItemStack.asNMSCopy(it))) }
    equipmentBuilder.boots?.let { list.add(DataPair(NMSEquipmentSlot.FEET, CraftItemStack.asNMSCopy(it))) }
    
    this.equipment = list
  }

  override fun animate(animation: EntityAnimation) {
    val id = when(animation) {
      EntityAnimation.SWING_MAIN_HAND -> 0
      EntityAnimation.HURT -> 1
      EntityAnimation.WAKE_UP -> 2
      EntityAnimation.SWING_OFF_HAND -> 3
      EntityAnimation.CRITICAL_HIT -> 4
      EntityAnimation.MAGIC_CRITICAL_HIT -> 5
    }
    val craftPlayer = player as CraftPlayer
    val packet = ClientboundAnimatePacket(craftPlayer.handle, id)
    packet.setFieldValue("id", entityId)
    craftPlayer.handle.connection.send(packet)
  }

  override fun move(location: Location, onGround: Boolean) {
    val packet = ClientboundTeleportEntityPacket(
      entityId,
      PositionMoveRotation(
        net.minecraft.world.phys.Vec3(location.x, location.y, location.z),
        net.minecraft.world.phys.Vec3.ZERO,
        location.yaw,
        location.pitch
      ),
      emptySet(),
      onGround
    )
    (player as CraftPlayer).handle.connection.send(packet)
  }

  override fun rotate(yaw: Float, pitch: Float, headRotation: Float?) {
    val connection = (player as CraftPlayer).handle.connection
    
    // Body and Pitch rotation
    val rotatePacket = ClientboundMoveEntityPacket.Rot(
      entityId,
      (yaw * 256.0f / 360.0f).toInt().toByte(),
      (pitch * 256.0f / 360.0f).toInt().toByte(),
      true
    )
    connection.send(rotatePacket)

    // Head rotation
    headRotation?.let {
      val headPacket = ClientboundRotateHeadPacket(player as net.minecraft.world.entity.Entity, (it * 256.0f / 360.0f).toInt().toByte())
      headPacket.setFieldValue("entityId", entityId)
      connection.send(headPacket)
    }
  }

  override fun updateMetadata() {
    val metadata = createMetadata()
    (player as CraftPlayer).handle.connection.send(ClientboundSetEntityDataPacket(entityId, metadata))
  }

  private fun createMetadata(): List<SynchedEntityData.DataValue<*>> {
    val metadata = mutableListOf<SynchedEntityData.DataValue<*>>()
    
    // Entity flags (Index 0)
    var flags: Byte = 0
    if (isInvisible) flags = (flags.toInt() or 0x20).toByte()
    if (isGlowing) flags = (flags.toInt() or 0x40).toByte()
    metadata.add(SynchedEntityData.DataValue(0, EntityDataSerializers.BYTE, flags))

    // Custom Name (Index 2)
    customName?.let {
        val component = PaperAdventure.asVanilla(Component.text(it))
        metadata.add(SynchedEntityData.DataValue(2, EntityDataSerializers.OPTIONAL_COMPONENT, Optional.of(component)))
    }
    
    // Custom Name Visible (Index 3)
    metadata.add(SynchedEntityData.DataValue(3, EntityDataSerializers.BOOLEAN, isCustomNameVisible))
    
    return metadata
  }

  override fun despawnAfter(ticks: Long) {
    val plugin = Bukkit.getPluginManager().getPlugin("DevCore") ?: return
    PluginRegistory.get(plugin).later(ticks.ticks) {
        val packet = ClientboundRemoveEntitiesPacket(entityId)
        (player as CraftPlayer).handle.connection.send(packet)
    }
  }

  override fun spawn() {
    val craftPlayer = player as CraftPlayer
    val connection = craftPlayer.handle.connection

    val spawnPacket = ClientboundAddEntityPacket(
        entityId,
        uuid,
        location.x,
        location.y,
        location.z,
        location.pitch,
        location.yaw,
        craftPlayer.handle.type, 
        0,
        net.minecraft.world.phys.Vec3.ZERO,
        location.yaw.toDouble()
    )
    connection.send(spawnPacket)

    // 2. Metadata Packet
    val metadata = createMetadata()
    val dataPacket = ClientboundSetEntityDataPacket(entityId, metadata)
    connection.send(dataPacket)

    if (equipment.isNotEmpty()) {
        connection.send(ClientboundSetEquipmentPacket(entityId, equipment))
    }
  }
}
