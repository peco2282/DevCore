# gui

[[English](README.md)] | [日本語]

Kotlin DSLと状態管理を備えた、Bukkit/Spigot向けのリアクティブなGUIフレームワークです。

## 特徴

- **宣言的なDSL**: Kotlinの型安全なビルダーを使用して、直感的にGUIを構築できます。
- **リアクティブな状態管理**: 状態（State）が変更されると、GUIが自動的に更新されます。
- **動的なタイトル**: タイトルの更新を自動的に処理します（インベントリを再作成し、閲覧中のプレイヤーに再提示します）。
- **ページネーションのサポート**: 内蔵の `PaginatedGuiContext` を使用して、ページ分けされたリストを簡単に作成できます。

## 使い方

### シンプルなGUI

```kotlin
val gui = inventory(rows = 3, title = component { text("My GUI") }) {
  slot(1, 1) {
    icon(Material.DIAMOND)
    name(component { text("クリックしてね") })
    onClick {
      player.sendMessage("ダイヤモンドをクリックしました！")
    }
  }
}
gui.open(player)
```

### 状態管理（State Management）

```kotlin
val gui = inventory(rows = 3, title = component { text("カウンター") }) {
  var count by state(0)

  slot(2, 5) {
    icon(Material.APPLE)
    name(component { text("カウント: $count") })
    onClick {
      count++ // GUIが自動的に更新されます！
    }
  }
}
```

### ページネーション（Pagination）

```kotlin
val items = (1..100).map { "アイテム #$it" }
val gui = paginatedInventory(rows = 6, title = component { text("アイテムリスト") }, items = items) {
  // コンテンツ表示エリアを定義
  content(9..44) { item ->
    icon(Material.PAPER)
    name(component { text(item) })
  }

  // 前のページ
  slot(6, 1) {
    icon(Material.ARROW)
    name(component { text("前へ") })
    onClick { prevPage() }
  }

  // 次のページ
  slot(6, 9) {
    icon(Material.ARROW)
    name(component { text("次へ") })
    onClick { nextPage() }
  }
}
```

## セットアップ

```kotlin
// プラグインのonEnableなどで一度だけリスナーを登録してください
GuiListener.register(plugin)
```
