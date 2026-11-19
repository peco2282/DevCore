package com.peco2282.devcore.anvilinput

import com.peco2282.devcore.util.versioned
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.event.inventory.InventoryType
import org.bukkit.plugin.Plugin

interface AnvilTextProvider { fun renameText(view: InventoryView): String? }

private val providers = versioned<AnvilTextProvider>("anvil input") {
  since("1.20.6", "com.peco2282.devcore.anvilinput.v1_20_6.AnvilTextProviderImpl")
  since("1.21", "com.peco2282.devcore.anvilinput.v1_21_4.AnvilTextProviderImpl")
  supportedBefore("1.22")
}

class AnvilInputManager(private val plugin: Plugin) : Listener, AutoCloseable {
  private data class Session(val callback: (String) -> Unit)
  private val sessions = ConcurrentHashMap<UUID, Session>()
  private val provider by lazy { providers.load() }

  init { Bukkit.getPluginManager().registerEvents(this, plugin) }

  fun ask(player: Player, title: Component, initial: String, callback: (String) -> Unit) {
    val inventory = Bukkit.createInventory(null, InventoryType.ANVIL, title)
    inventory.setItem(0, ItemStack(Material.PAPER).apply {
      itemMeta = itemMeta.apply { displayName(Component.text(initial)) }
    })
    sessions[player.uniqueId] = Session(callback)
    player.openInventory(inventory)
  }

  @EventHandler fun onClick(event: InventoryClickEvent) {
    if (event.rawSlot != 2) return
    val session = sessions[event.whoClicked.uniqueId] ?: return
    event.isCancelled = true
    val input = provider.renameText(event.view)?.trim().orEmpty()
    if (input.isEmpty()) return
    sessions.remove(event.whoClicked.uniqueId)
    event.whoClicked.closeInventory()
    session.callback(input)
  }

  @EventHandler fun onClose(event: InventoryCloseEvent) { sessions.remove(event.player.uniqueId) }
  override fun close() { sessions.clear(); HandlerList.unregisterAll(this) }
}

private val managers = ConcurrentHashMap<Plugin, AnvilInputManager>()
fun Plugin.anvilInputs(): AnvilInputManager = managers.computeIfAbsent(this, ::AnvilInputManager)
fun Player.askText(plugin: Plugin, title: Component, initial: String = "", callback: (String) -> Unit) =
  plugin.anvilInputs().ask(this, title, initial, callback)
