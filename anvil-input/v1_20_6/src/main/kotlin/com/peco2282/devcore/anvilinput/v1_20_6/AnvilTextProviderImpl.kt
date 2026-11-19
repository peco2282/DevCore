package com.peco2282.devcore.anvilinput.v1_20_6

import com.peco2282.devcore.anvilinput.AnvilTextProvider
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.InventoryView

class AnvilTextProviderImpl : AnvilTextProvider {
  override fun renameText(view: InventoryView): String? = (view.topInventory as AnvilInventory).renameText
}
