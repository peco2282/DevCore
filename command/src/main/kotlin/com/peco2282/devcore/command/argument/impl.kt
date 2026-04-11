package com.peco2282.devcore.command.argument

import com.peco2282.devcore.util.DevCoreInternal

@DevCoreInternal
object Impl {
  data class ColumnBlockPositionImpl(
    override val blockX: Int,
    override val blockZ: Int
  ): ColumnBlockPosition

  data class ColumnFinePositionImpl(
    override val x: Double,
    override val z: Double
  ): ColumnFinePosition

  data class RotationImpl(
    override val yaw: Float,
    override val pitch: Float
  ): Rotation
}