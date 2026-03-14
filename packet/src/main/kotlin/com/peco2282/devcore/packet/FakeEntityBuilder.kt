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

@DslMarker
annotation class FakeVisualDsl

/**
 * プレイヤーに対してフェイク演出を送るエントリーポイント
 */
inline fun Player.sendFakeVisuals(block: FakeVisualContext.() -> Unit) {
  FakeVisualContext(this).apply(block)
}

class FakeVisualContext(val player: Player) {
  // エンティティ生成のDSL
  inline fun spawnEntity(
    type: EntityType,
    loc: Location,
    entityId: Int = (System.currentTimeMillis() % 100000).toInt(), // 重複を避ける簡易ID
    block: FakeEntityBuilder.() -> Unit
  ) {
    val builder = FakeEntityBuilder(type, entityId).apply(block)
    builder.send(player, loc)
  }

  // ブロック変更のDSL
  fun setFakeBlock(loc: Location, material: Material) {
    player.sendBlockChange(loc, material.createBlockData())
  }
}

@FakeVisualDsl
class FakeEntityBuilder(val type: EntityType, val entityId: Int) {
  var customName: String? = null
  var isGlowing: Boolean = false
  var isInvisible: Boolean = false

  private val equipment = mutableMapOf<EquipmentSlot, ItemStack>()

  fun equipment(block: EquipmentBuilder.() -> Unit) {
    equipment.putAll(EquipmentBuilder().apply(block).items)
  }

  // パケットの組み立てと送信
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

    // 2. Metadata (Entityの状態)
    val metadata = mutableListOf<EntityData<*>>()

    // 基本ステータス (Index 0)
    var bitmask = 0x00.toByte()
    if (isInvisible) bitmask = (bitmask.toInt() or 0x20).toByte()
    if (isGlowing) bitmask = (bitmask.toInt() or 0x40).toByte()
    metadata.add(EntityData(0, EntityDataTypes.BYTE, bitmask))

    // カスタムネーム
    customName?.let {
      metadata.add(EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(it)))
      metadata.add(EntityData(3, EntityDataTypes.BOOLEAN, true)) // 名前を表示するか
    }

    val metadataPacket = WrapperPlayServerEntityMetadata(entityId, metadata)

    // 送信
    user.sendPacket(spawnPacket)
    user.sendPacket(metadataPacket)

    // 3. 装備パケットの送信
    if (equipment.isNotEmpty()) {
      val items = equipment.map { (slot, item) ->
        Equipment(
          slot, // 変換用拡張関数
          SpigotConversionUtil.fromBukkitItemStack(item)
        )
      }
      user.sendPacket(WrapperPlayServerEntityEquipment(entityId, items))
    }
  }
}
