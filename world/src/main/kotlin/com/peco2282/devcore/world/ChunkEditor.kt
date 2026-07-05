package com.peco2282.devcore.world

import org.bukkit.Chunk
import org.bukkit.Material

/**
 * Interface for editing Minecraft chunks with DSL support.
 *
 * Provides methods to fill, replace blocks, and refresh chunks.
 */
@WorldDsl
interface ChunkEditor {
  /**
   * The chunk being edited.
   */
  val chunk: Chunk

  /**
   * Fills the entire chunk with the specified material.
   *
   * @param material The material to fill the chunk with.
   */
  fun fill(material: Material)

  /**
   * Replaces all blocks of one material with another material in the chunk.
   *
   * @param from The material to replace.
   * @param to The material to replace with.
   */
  fun replace(from: Material, to: Material)

  /**
   * Refreshes the chunk to apply changes on the client side.
   */
  fun refresh()
}

/**
 * Internal implementation of [ChunkEditor].
 *
 * @property chunk The chunk to be edited.
 */
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
 * Extension function for [Chunk] to edit it using a DSL builder.
 *
 * This function provides a convenient DSL-based approach to modify chunks in Minecraft worlds.
 * It creates a [ChunkEditor] instance for the chunk and applies the provided actions.
 *
 * ## Usage Examples
 *
 * ### Basic Usage - Fill a chunk
 * ```kotlin
 * val chunk: Chunk = world.getChunkAt(0, 0)
 * chunk.edit {
 *   fill(Material.STONE)
 * }
 * ```
 *
 * ### Replace blocks in a chunk
 * ```kotlin
 * chunk.edit {
 *   replace(from = Material.DIRT, to = Material.GRASS_BLOCK)
 * }
 * ```
 *
 * ### Multiple operations
 * ```kotlin
 * chunk.edit {
 *   fill(Material.STONE)
 *   replace(from = Material.STONE, to = Material.DIAMOND_ORE)
 *   refresh() // Apply changes to clients
 * }
 * ```
 *
 * ### Using with World DSL
 * ```kotlin
 * world.edit {
 *   chunk(x = 0, z = 0) {
 *     fill(Material.BEDROCK)
 *     refresh()
 *   }
 * }
 * ```
 *
 * ## Available Operations
 *
 * - `fill(material: Material)` - Fills the entire chunk (all 16x16 columns from minHeight to maxHeight) with the specified material
 * - `replace(from: Material, to: Material)` - Replaces all blocks matching the 'from' material with the 'to' material
 * - `refresh()` - Forces the chunk to be resent to all players, making changes visible immediately
 *
 * ## Performance Considerations
 *
 * - Operations modify blocks synchronously and can be expensive for large-scale changes
 * - Consider using `refresh()` only once after all modifications to minimize network traffic
 * - The chunk editor operates on blocks from world minHeight to maxHeight (typically -64 to 320 in 1.18+)
 *
 * @param action The DSL builder action to apply to the chunk. Receives [ChunkEditor] as the receiver.
 * @see ChunkEditor
 * @see WorldEditor.chunk
 */
fun Chunk.edit(action: ChunkEditor.() -> Unit) {
  ChunkEditorImpl(this).apply(action)
}
