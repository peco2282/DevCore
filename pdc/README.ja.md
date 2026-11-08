# DevCore PDC

[[English](README.md)] | [日本語]

Bukkit の `PersistentDataContainer` を型安全に扱います。`NamespacedKey` と `PersistentDataType` の組を揃えて持つ必要がなく、
自動で保存されるプロパティも使えます。

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:pdc:<version>") }
```

## 型付きキー

キーが値の型を持つので、読み込みと書き込みで型が食い違うことがありません。

```kotlin
val KILLS = pdcKey<Int>(plugin, "kills")
val OWNER = pdcKey<UUID>(plugin, "owner")

player[KILLS] = 3                       // PersistentDataHolder なら何でも: Player, Entity, Chunk, World, タイルエンティティ
val kills: Int? = player[KILLS]
if (KILLS in player) player.remove(KILLS)

player.persistentDataContainer.update(KILLS, default = 0) { it + 1 }   // 読み取り→変更→書き込み
container.getOrPut(OWNER) { player.uniqueId }
```

標準で使える型: `String`、`Int`、`Long`、`Double`、`Float`、`Short`、`Byte`、`Boolean`、
`ByteArray`、`IntArray`、`LongArray`、`UUID`、任意の `enum`、およびこれらのプリミティブの `List<T>`。
それ以外はキーを作った時点でエラーになります(後の読み込み時ではありません)。

```kotlin
PdcTypes.registerString(Color3::class, { "${it.r},${it.g},${it.b}" }, { /* パース */ })
val TINT = pdcKey<Color3>(plugin, "tint")
```

## 永続化されるプロパティ

```kotlin
var Player.kills: Int by pdc(plugin, "kills", default = 0)
var Player.nickname: String? by pdcOrNull(plugin, "nickname")   // null を代入すると削除されます

player.kills++
```

拡張プロパティはトップレベルに宣言してください。その時点でプラグインのインスタンスが無い場合は、
`NamespacedKey` を渡せます: `pdc(NamespacedKey.fromString("myplugin:kills")!!, 0)`。

## アイテムとブロック

`ItemMeta` はコピーなので、アイテムのコンテナへの書き込みは通常、取得・変更・再設定が必要です。ここでは不要です。

```kotlin
item[OWNER] = player.uniqueId
item.editPdc { this[OWNER] = id; this[KILLS] = 0 }       // 複数の書き込みを1回のメタ更新で
var ItemStack.level: Int by itemPdc(plugin, "level", 1)

block.editPdc { this[OWNER] = id }                       // チェスト、看板、かまど等。通常のブロックでは false
```

通常のブロック(石、土など)はデータを持てません。チャンクに座標をキーとして保存してください。
