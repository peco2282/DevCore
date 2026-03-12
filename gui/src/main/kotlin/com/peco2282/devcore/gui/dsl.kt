package com.peco2282.devcore.gui

import net.kyori.adventure.text.Component
import org.bukkit.inventory.Inventory

/**
 * DSLを使用してGUIを作成します。
 * @param title GUIのタイトル
 * @param rows GUIの行数 (1-6)
 * @param creator 構築用のラムダ
 * @return 生成された Inventory
 */
fun gui(title: Component, rows: Int = 6, creator: GuiCreator.() -> Unit): Inventory {
  val builder = GuiCreator(rows)
  builder.title = title
  builder.creator()
  return builder.create()
}

/**
 * 宣言的UIを使用してGUIを作成します。
 */
fun inventory(rows: Int = 3, title: Component, builder: GuiContext.() -> Unit): Gui {
  return object : GuiContext(rows) {
    override fun build() {
      title(title)
      this.builder()
    }
  }
}

/**
 * 他の構成をテンプレートとして含めます。
 */
fun GuiCreator.template(template: GuiCreator.() -> Unit) {
  this.template()
}

/**
 * 他の構成をテンプレートとして含めます。
 */
fun GuiContext.template(template: GuiContext.() -> Unit) {
  this.template()
}

/**
 * 複数のスロットにアイテムを一括で配置するテンプレート
 */
fun GuiCreator.fill(material: org.bukkit.Material, pickable: Boolean = false, creator: SlotCreator.() -> Unit = {}) {
  for (i in 0 until (rows * 9)) {
    slot(i) {
      icon(material)
      pickable(pickable)
      creator()
    }
  }
}