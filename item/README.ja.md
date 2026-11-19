# DevCore Item

[[English](README.md)] | [日本語]

カスタムアイテムを一箇所で定義します。見た目、振る舞い、クラフトレシピをまとめて書けます。
スタックには隠し ID が付くので、プレイヤーが名前を変えても識別でき、振る舞いは散らばったリスナーではなくアイテムに紐づきます。

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:item:<version>") }
```

`pdc`(隠し ID 用)と `util`(アイテムビルダー用)を使います。

## 例

```kotlin
val items = plugin.customItems()

val ruby = items.register("ruby_sword", Material.DIAMOND_SWORD) {
  appearance {
    displayName = Component.text("ルビーの剣", NamedTextColor.RED)
    enchant(Enchantment.SHARPNESS, 3)
  }
  onAttack { it.damage *= 1.5 }
  onRightClick { it.player.sendMessage("ブン！") }
  shaped(" R", " R", " S") {
    ingredient('R', Material.REDSTONE_BLOCK)
    ingredient('S', Material.STICK)
  }
}

ruby.give(player)            // 入りきらない分はプレイヤーの足元にドロップされます
ruby.matches(stack)          // 名前を変えられていても true
items.of(stack)              // スタックが表す CustomItem、なければ null
```

## 振る舞い

ハンドラはアイテムがプレイヤーの**メインハンド**にあるときだけ動きます(消費・ドロップ・設置は、そのイベントで扱われるアイテムが対象です)。

| 関数 | イベント |
|---|---|
| `onRightClick` / `onLeftClick` | `PlayerInteractEvent` |
| `onAttack` | `EntityDamageByEntityEvent`(攻撃者がプレイヤー) |
| `onBreak` | `BlockBreakEvent` |
| `onPlace` | `BlockPlaceEvent` |
| `onConsume` | `PlayerItemConsumeEvent` |
| `onDrop` | `PlayerDropItemEvent` |

- 元のイベントが渡されるので、キャンセルや変更ができます(`it.isCancelled = true`、`it.damage = ...`)。
- 1つのハンドラで例外が出てもログに記録され、他のハンドラは止まりません。
- `placeable = false` で、ブロックの素材(例: `DIRT`)を使うアイテムの設置を禁止できます。

## レシピ

`shaped(行..) { ingredient(記号, material) }` と `shapeless { ingredient(material, 個数) }`。材料には `ItemStack` も指定でき、
たとえば別のカスタムアイテムを `ingredient('R', ruby.create())` のように使えます。

レシピはアイテム登録時に検証されます(未知の記号、材料のない記号、形状のサイズ違いなど)。

## ライフサイクル

`items.close()` でレシピを削除し、イベント処理を止めます。既存のスタックは ID を保持します。
他のプラグインのレジストリのアイテムが、あなたのレジストリで一致することはありません。
