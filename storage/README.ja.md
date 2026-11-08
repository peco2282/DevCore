# DevCore Storage

[[English](README.md)] | [日本語]

プレイヤーごとのデータを、参加前に読み込み、退出時に保存します。メインスレッドをブロックしません。
データベースが不要な小〜中規模のプラグイン向けです。

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies { implementation("com.peco2282.devcore:storage:<version>") }
```

## 例

```kotlin
data class Stats(var kills: Int = 0, var coins: Long = 0, var tags: List<String> = emptyList())

val stats = plugin.playerData<Stats>()          // plugins/<plugin>/playerdata/<uuid>.yml

plugin.on<PlayerJoinEvent> {
  handle { stats[player].coins += 10 }          // 普通のオブジェクト操作。ここでは I/O しません
}
```

data class は `config` モジュールで YAML に対応付けられます。後のバージョンで追加したフィールドは、古いファイルを読んだときに既定値になります。

## 自動で行われること

| タイミング | 内容 |
|---|---|
| `AsyncPlayerPreLoginEvent` | メインスレッド以外でファイルを読みます。`PlayerJoinEvent` の時点で準備済みです。 |
| 退出時 | メインスレッドでスナップショットを取り、非同期で書き込み、メモリから外します。 |
| `autosave` ごと(既定 5 分) | オンライン全員を保存します。 |
| プラグイン無効化時 | 全員を保存し、書き込みの完了を待ちます。 |
| 退出直後の再ログイン | 保存中の書き込みを待つので、古いデータを読むことはありません。 |

書き込みは一時ファイルに行ってから置き換えるので、クラッシュしても書きかけのファイルが残りません。

## オフラインのプレイヤー

```kotlin
stats.peek(uuid).thenAccept { data -> /* データが無ければ null */ }   // キャッシュせずに読み取り
stats.edit(uuid) { it.coins += 100 }                                    // オンラインでもオフラインでも変更して保存
stats.delete(uuid)                                                      // オフラインのプレイヤーのみ
```

オフラインのプレイヤーに対する `edit` はストレージ用スレッドで動きます。中で Bukkit API を使わないでください。

## データを読めないとき

既定では、プレイヤーを**ログイン時に拒否**し(`LoadFailure.KICK`)、壊れたファイルを `<uuid>.yml.corrupt-<時刻>` に改名します。
既定値のまま遊ばせると、次の保存で本来のデータを上書きしてしまうためです。
`LoadFailure.USE_DEFAULT` は既定値で続行し、改名したファイルを復旧用に残します。

## 他の保存先

`DataBackend<V>`(テキストを読み書きする小さなメソッド 5 つ)を実装すると、データベースや Redis に保存できます。
`plugin.playerData(backend, default = { Stats() })` で使います。

## ルール

- データの変更はメインスレッドから行ってください。保存用のスナップショットもそこで取られます。
- データの読み込み前(自前のプレログイン処理など)に `stats[player]` を呼ぶと例外になります。
