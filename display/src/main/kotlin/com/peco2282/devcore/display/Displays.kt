package com.peco2282.devcore.display

import com.peco2282.devcore.scheduler.taskTimer
import com.peco2282.devcore.scheduler.TaskHandle
import com.peco2282.devcore.scheduler.ticks
import kotlin.math.PI
import kotlin.time.Duration
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/** Common transformation and interpolation options for display entities. */
open class DisplayBuilder {
  var scale: Float = 1f
  var billboard: Display.Billboard = Display.Billboard.FIXED
  var interpolationDuration: Int = 0
  var interpolationDelay: Int = 0
  internal var spinPeriod: Duration? = null

  fun spin(period: Duration) {
    require(period.isPositive()) { "Spin period must be positive" }
    spinPeriod = period
  }

  internal fun applyTo(display: Display) {
    require(scale.isFinite() && scale >= 0f) { "Scale must be finite and non-negative" }
    require(interpolationDuration >= 0) { "Interpolation duration must be non-negative" }
    display.billboard = billboard
    display.interpolationDuration = interpolationDuration
    display.interpolationDelay = interpolationDelay
    display.transformation = display.transformation.apply { this.scale.set(scale) }
    display.isPersistent = false
  }
}

class ItemDisplayBuilder : DisplayBuilder() {
  /** Item rendered by the display. */
  lateinit var item: ItemStack
  internal fun requireItem(): ItemStack {
    check(::item.isInitialized) { "Item display requires an item" }
    return item
  }
}

class BlockDisplayBuilder : DisplayBuilder() {
  /** Block data rendered by the display. */
  lateinit var block: BlockData
  internal fun requireBlock(): BlockData {
    check(::block.isInitialized) { "Block display requires block data" }
    return block
  }
}

/** Spawns a lifecycle-owned item display at [location]. */
fun Plugin.itemDisplay(location: Location, block: ItemDisplayBuilder.() -> Unit): ItemDisplay {
  val config = ItemDisplayBuilder().apply(block)
  val item = config.requireItem()
  val world = requireNotNull(location.world)
  val display = world.spawn(location, ItemDisplay::class.java) { entity ->
    config.applyTo(entity)
    entity.itemStack = item.clone()
  }
  DisplayLifecycle.of(this).track(display)
  scheduleSpin(display, config.spinPeriod)
  return display
}

/** Spawns a lifecycle-owned block display at [location]. */
fun Plugin.blockDisplay(location: Location, block: BlockDisplayBuilder.() -> Unit): BlockDisplay {
  val config = BlockDisplayBuilder().apply(block)
  val blockData = config.requireBlock()
  val world = requireNotNull(location.world)
  val display = world.spawn(location, BlockDisplay::class.java) { entity ->
    config.applyTo(entity)
    entity.block = blockData
  }
  DisplayLifecycle.of(this).track(display)
  scheduleSpin(display, config.spinPeriod)
  return display
}

private fun Plugin.scheduleSpin(display: Display, period: Duration?) {
  if (period == null) return
  val radians = (2.0 * PI / (period.inWholeMilliseconds / 50.0)).toFloat()
  lateinit var task: TaskHandle
  task = display.world.taskTimer(this, 0.ticks, 1.ticks) {
    if (!display.isValid) {
      task.cancel()
      return@taskTimer
    }
    display.transformation = display.transformation.apply { leftRotation.rotateY(radians) }
  }
}
