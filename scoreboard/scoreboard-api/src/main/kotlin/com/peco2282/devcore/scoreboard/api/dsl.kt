package com.peco2282.devcore.scoreboard.api

import com.peco2282.devcore.scheduler.Ticks
import com.peco2282.devcore.scoreboard.api.builder.BossBarBuilder
import com.peco2282.devcore.scoreboard.api.builder.SidebarBuilder
import net.kyori.adventure.text.Component
import org.bukkit.plugin.Plugin

inline fun sidebar(title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar({ title }, block)

/**
 * サイドバーを構築するDSL
 */
inline fun sidebar(noinline title: () -> Component, block: SidebarBuilder.() -> Unit): SidebarHandle {
  return SidebarBuilder(title).apply(block).build()
}

inline fun sidebar(plugin: Plugin, title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar(plugin, { title }, block)

inline fun sidebar(plugin: Plugin, noinline title: () -> Component, block: SidebarBuilder.() -> Unit): SidebarHandle {
  return SidebarBuilder(title).apply { autoRefresh(plugin) }.apply(block).build()
}

inline fun sidebar(plugin: Plugin, refreshInterval: Ticks, title: Component, block: SidebarBuilder.() -> Unit): SidebarHandle =
  sidebar(plugin, refreshInterval, { title }, block)

inline fun sidebar(plugin: Plugin, refreshInterval: Ticks, noinline title: () -> Component, block: SidebarBuilder.() -> Unit): SidebarHandle {
  return SidebarBuilder(title).apply { autoRefresh(plugin, refreshInterval) }.apply(block).build()
}

inline fun bossBar(block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply(block).build()

inline fun bossBar(title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar({ title }, block)

inline fun bossBar(noinline title: () -> Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { title(title) }.apply(block).build()

inline fun bossBar(plugin: Plugin, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { autoRefresh(plugin) }.apply(block).build()

inline fun bossBar(plugin: Plugin, title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar(plugin, { title }, block)

inline fun bossBar(plugin: Plugin, noinline title: () -> Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply {
    title(title)
    autoRefresh(plugin)
  }.apply(block).build()

inline fun Plugin.bossBar(refreshInterval: Ticks, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply { autoRefresh(this@bossBar, refreshInterval) }.apply(block).build()

inline fun Plugin.bossBar(refreshInterval: Ticks, title: Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  bossBar(refreshInterval, { title }, block)

inline fun Plugin.bossBar(refreshInterval: Ticks, noinline title: () -> Component, block: BossBarBuilder.() -> Unit): BossBarHandle =
  BossBarBuilder().apply {
    title(title)
    autoRefresh(this@bossBar, refreshInterval)
  }.apply(block).build()
