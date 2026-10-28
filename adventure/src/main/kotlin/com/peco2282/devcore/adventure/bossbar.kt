package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component

class BossBarBuilder {
  var name: Component = Component.empty()
  var progress: Float = 1.0f
  var color: BossBar.Color = BossBar.Color.PINK
  var overlay: BossBar.Overlay = BossBar.Overlay.PROGRESS
  var flags: Set<BossBar.Flag> = emptySet()

  fun name(consumer: ComponentBuilder.() -> Unit) {
    name = component(consumer)
  }

  fun flag(flag: BossBar.Flag, enabled: Boolean = true) {
    flags = if (enabled) flags + flag else flags - flag
  }

  internal fun build(): BossBar {
    require(progress.isFinite() && progress in 0.0f..1.0f) { "Boss bar progress must be finite and between 0 and 1" }
    return BossBar.bossBar(name, progress, color, overlay, flags)
  }
}

fun bossBar(builder: BossBarBuilder.() -> Unit): BossBar = BossBarBuilder().apply(builder).build()

fun BossBar.update(builder: BossBarBuilder.() -> Unit): BossBar {
  val updated = BossBarBuilder().apply {
    name = this@update.name()
    progress = this@update.progress()
    color = this@update.color()
    overlay = this@update.overlay()
    flags = this@update.flags()
  }.apply(builder).build()

  name(updated.name())
  progress(updated.progress())
  color(updated.color())
  overlay(updated.overlay())

  val oldFlags = flags()
  val newFlags = updated.flags()
  (oldFlags - newFlags).forEach { removeFlag(it) }
  (newFlags - oldFlags).forEach { addFlag(it) }
  return this
}

fun Audience.showBossBar(builder: BossBarBuilder.() -> Unit): BossBar = bossBar(builder).also { showBossBar(it) }

fun Collection<Audience>.showBossBar(bossBar: BossBar) = forEach { it.showBossBar(bossBar) }

fun Collection<Audience>.showBossBar(builder: BossBarBuilder.() -> Unit): BossBar {
  val bossBar = bossBar(builder)
  forEach { it.showBossBar(bossBar) }
  return bossBar
}

fun Collection<Audience>.hideBossBar(bossBar: BossBar) = forEach { it.hideBossBar(bossBar) }