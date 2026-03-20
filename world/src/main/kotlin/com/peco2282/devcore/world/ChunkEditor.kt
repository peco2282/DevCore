package com.peco2282.devcore.world

import org.bukkit.Chunk
import org.bukkit.Material

@WorldDsl
interface ChunkEditor {
  val chunk: Chunk

  /**
   * チャンク内のすべてのブロックを置換します。
   */
  fun fill(material: Material)

  /**
   * 特定の条件に一致するブロックを置換します。
   */
  fun replace(from: Material, to: Material)

  /**
   * チャンクを再生成します。
   */
  fun refresh()
}

internal class ChunkEditorImpl(override val chunk: Chunk) : ChunkEditor {
  override fun fill(material: Material) {
    for (x in 0..15) {
      for (z in 0..15) {
        for (y in chunk.world.minHeight until chunk.world.maxHeight) {
          chunk.getBlock(x, y, z).type = material
        }
      }
    }
  }

  override fun replace(from: Material, to: Material) {
    for (x in 0..15) {
      for (z in 0..15) {
        for (y in chunk.world.minHeight until chunk.world.maxHeight) {
          val block = chunk.getBlock(x, y, z)
          if (block.type == from) {
            block.type = to
          }
        }
      }
    }
  }

  override fun refresh() {
    chunk.world.refreshChunk(chunk.x, chunk.z)
  }
}

/**
 * チャンクの設定をDSLで編集します。
 */
fun Chunk.edit(action: ChunkEditor.() -> Unit) {
  ChunkEditorImpl(this).apply(action)
}
