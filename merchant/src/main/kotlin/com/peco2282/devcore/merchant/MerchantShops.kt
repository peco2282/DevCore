package com.peco2282.devcore.merchant

import io.papermc.paper.event.player.PlayerPurchaseEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin
import org.bukkit.inventory.MerchantRecipe
import java.util.WeakHashMap

/** Opens a lifecycle-managed custom merchant for this player. */
fun Player.openShop(plugin: Plugin, title: Component, block: ShopBuilder.() -> Unit) {
  plugin.openShop(this, title, block)
}

/** Opens a lifecycle-managed custom merchant for [player]. */
fun Plugin.openShop(player: Player, title: Component, block: ShopBuilder.() -> Unit) {
  val definition = shopDefinition(block)
  val merchant = Bukkit.createMerchant(title)
  merchant.recipes = definition.trades.map { it.recipe }
  MerchantCallbacks.forPlugin(this).track(definition)
  player.openMerchant(merchant, true)
}

private class MerchantCallbacks(private val plugin: Plugin) : Listener {
  private val callbacks = WeakHashMap<MerchantRecipe, (PlayerPurchaseEvent) -> Unit>()

  fun track(shop: ShopDefinition) {
    shop.trades.forEach { trade -> trade.onTrade?.let { callbacks[trade.recipe] = it } }
  }

  @EventHandler(ignoreCancelled = true)
  fun onPurchase(event: PlayerPurchaseEvent) { callbacks[event.trade]?.invoke(event) }

  @EventHandler
  fun onDisable(event: PluginDisableEvent) { if (event.plugin == plugin) callbacks.clear() }

  companion object {
    private val listeners = WeakHashMap<Plugin, MerchantCallbacks>()
    fun forPlugin(plugin: Plugin): MerchantCallbacks = synchronized(listeners) {
      listeners.getOrPut(plugin) {
        MerchantCallbacks(plugin).also { plugin.server.pluginManager.registerEvents(it, plugin) }
      }
    }
  }
}
