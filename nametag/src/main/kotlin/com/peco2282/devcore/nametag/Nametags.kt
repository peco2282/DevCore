package com.peco2282.devcore.nametag

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.WeakHashMap

/** Handle for a periodically updated tab header and footer. */
class TabListHandle internal constructor(
  private val plugin: Plugin,
  private val header: (Player) -> Component,
  private val footer: (Player) -> Component,
  refreshTicks: Long
) {
  private val task: BukkitTask
  private var removed = false

  init {
    require(refreshTicks > 0) { "refreshTicks must be positive" }
    task = plugin.server.scheduler.runTaskTimer(plugin, Runnable(::update), 0L, refreshTicks)
  }

  fun update() {
    if (!removed) plugin.server.onlinePlayers.forEach { it.sendPlayerListHeaderAndFooter(header(it), footer(it)) }
  }

  fun remove(clear: Boolean = true) {
    if (removed) return
    removed = true
    task.cancel()
    if (clear) plugin.server.onlinePlayers.forEach {
      it.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty())
    }
  }
}

/** Builder for viewer-specific tab list content. */
class TabListBuilder {
  private var header: (Player) -> Component = { Component.empty() }
  private var footer: (Player) -> Component = { Component.empty() }
  var refreshTicks: Long = 20L

  fun header(component: Component) { header = { component } }
  fun header(block: (Player) -> Component) { header = block }
  fun footer(component: Component) { footer = { component } }
  fun footer(block: (Player) -> Component) { footer = block }

  internal fun build(plugin: Plugin): TabListHandle = TabListHandle(plugin, header, footer, refreshTicks)
}

/** Lifecycle owner for nametags and tab lists. */
class NametagManager internal constructor(private val plugin: Plugin, refreshTicks: Long) : Listener {
  private val tags = linkedMapOf<UUID, NametagConfig>()
  private val tabLists = mutableSetOf<TabListHandle>()
  private val refreshTask: BukkitTask

  init {
    require(refreshTicks > 0) { "refreshTicks must be positive" }
    plugin.server.pluginManager.registerEvents(this, plugin)
    refreshTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable(::refresh), 0L, refreshTicks)
  }

  fun set(player: Player, block: NametagConfig.() -> Unit) {
    tags[player.uniqueId] = NametagConfig().apply(block)
    refresh()
  }

  fun remove(player: Player) {
    tags.remove(player.uniqueId)
    plugin.server.onlinePlayers.map { it.scoreboard }.distinct().forEach { scoreboard ->
      scoreboard.getEntryTeam(player.name)?.let { team ->
        team.removeEntry(player.name)
        if (team.entries.isEmpty()) team.unregister()
      }
    }
  }

  fun refresh() {
    val players = plugin.server.onlinePlayers.associateBy { it.uniqueId }
    val scoreboards = players.values.map { it.scoreboard }.distinct()
    tags.forEach { (id, config) ->
      val player = players[id] ?: return@forEach
      scoreboards.forEach { applyNametag(it, id, player.name, config) }
    }
  }

  fun tabList(block: TabListBuilder.() -> Unit): TabListHandle =
    TabListBuilder().apply(block).build(plugin).also(tabLists::add)

  @EventHandler fun onJoin(event: PlayerJoinEvent) { refresh() }
  @EventHandler fun onQuit(event: PlayerQuitEvent) { remove(event.player) }
  @EventHandler fun onDisable(event: PluginDisableEvent) {
    if (event.plugin != plugin) return
    refreshTask.cancel()
    tabLists.forEach { it.remove(clear = false) }
    tags.keys.mapNotNull(plugin.server::getPlayer).forEach(::remove)
  }

  companion object {
    private val managers = WeakHashMap<Plugin, NametagManager>()
    internal fun forPlugin(plugin: Plugin, refreshTicks: Long): NametagManager = synchronized(managers) {
      managers.getOrPut(plugin) { NametagManager(plugin, refreshTicks) }
    }
  }
}

/** Returns this plugin's nametag manager. The first call fixes [refreshTicks]. */
fun Plugin.nametags(refreshTicks: Long = 20L): NametagManager = NametagManager.forPlugin(this, refreshTicks)

/** Configures a player through this plugin's manager. */
fun Plugin.nametag(player: Player, block: NametagConfig.() -> Unit) = nametags().set(player, block)

/** Creates a viewer-specific, periodically updated tab list. */
fun Plugin.tablist(block: TabListBuilder.() -> Unit): TabListHandle = nametags().tabList(block)
