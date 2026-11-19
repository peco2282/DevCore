package com.peco2282.devcore.anvilinput.v1_21_4

import com.peco2282.devcore.anvilinput.AnvilTextProvider
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.view.AnvilView

class AnvilTextProviderImpl : AnvilTextProvider {
  override fun renameText(view: InventoryView): String? = (view as AnvilView).renameText
}
