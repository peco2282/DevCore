package com.peco2282.devcore.packet

import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KProperty

@FakeVisualDsl
class EquipmentBuilder {
  // 実際に値が設定されたスロットだけを保持する
  private val _items = mutableMapOf<EquipmentSlot, ItemStack>()
  internal val items: Map<EquipmentSlot, ItemStack> get() = _items

  // 各スロットへのショートカットプロパティ
  var mainHand: ItemStack? by EquipmentSlot.MAIN_HAND.delegate()
  var offHand: ItemStack? by EquipmentSlot.OFF_HAND.delegate()
  var helmet: ItemStack? by EquipmentSlot.HELMET.delegate()
  var chestplate: ItemStack? by EquipmentSlot.CHEST_PLATE.delegate()
  var leggings: ItemStack? by EquipmentSlot.LEGGINGS.delegate()
  var boots: ItemStack? by EquipmentSlot.BOOTS.delegate()

  // 委譲プロパティ用の内部クラス：nullを代入するとMapから削除し、軽量化に貢献
  private inner class EquipmentMapDelegate(val slot: EquipmentSlot) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack? = _items[slot]
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack?) {
      if (value == null || value.type == Material.AIR) {
        _items.remove(slot)
      } else {
        _items[slot] = value
      }
    }
  }

  // 拡張関数で .delegate() と書けるようにして記述をスッキリさせる
  private fun EquipmentSlot.delegate() = EquipmentMapDelegate(this)
}