package com.peco2282.devcore.packet

import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector

interface PacketHub {
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
}