# DevCore Effect

[[English](README.md)] | [日本語]

再利用できるパーティクル/サウンドのエフェクト、形状ジェネレータ、エンティティに追従できる繰り返し再生を提供します。
`sin`/`cos` のループや `BukkitRunnable` のカウンタを手で書く必要がなくなります。

## 導入方法 (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:effect:<version>")
}
```

`core` に含まれます。`scheduler` に依存します(再生で `Ticks` と `TaskHandle` を公開するため `api` です)。

## クイックスタート

```kotlin
// 一度作ってフィールドに保持し、好きなときに再生します。
val levelUp = effect {
  particle(Particle.END_ROD, circle(1.5).offset(y = 1.0))
  particle(Particle.DUST, sphere(0.6).offset(y = 1.0), data = dust(Color.AQUA, 1.2f))
  sound(Sound.ENTITY_PLAYER_LEVELUP)
}

levelUp.play(player.location)                    // 1回、周囲の全員に
levelUp.play(player.location, listOf(player))    // 1回、指定プレイヤーだけに
```

## 形状 (Shape)

`Shape` は原点からのオフセットの集合です。サーバーに依存しない純粋な幾何なので、テストも簡単です。

| 関数 | 結果 |
|---|---|
| `point()` | 原点の1点 |
| `line(to, density = 0.25)` | 原点から `to` への線。`density` は点の間隔 |
| `circle(radius, points = 36, normal = UP, arc = 2π, start = 0.0)` | `normal` に垂直な面上の円または円弧 |
| `sphere(radius, points = 100)` | 球面上に均等に分布した点 |
| `helix(radius, height, turns = 3.0, points = 60)` | +Y 方向に昇るらせん |

変形や結合ができ、どの操作も新しい Shape を返します。

```kotlin
circle(1.0).rotateX(PI / 4).offset(y = 1.0)   // 頭の高さの傾いた輪
helix(0.5, 2.0) + circle(1.0)                  // 2つの形状を結合
shape.scale(2.0).rotateY(angle)
```

## 繰り返し再生

`playEffect` は `every` ティックごとに、`duration` の間(または返されたハンドルをキャンセルするまで)エフェクトを再生します。

```kotlin
// 固定位置
plugin.playEffect(effect, location, every = 2.ticks, duration = 5.seconds)

// エンティティに追従。エンティティが消える/プレイヤーがログアウトすると自動で止まります
plugin.playEffect(effect, player, every = 2.ticks, duration = 5.seconds)

// 動く任意の点。null を返すと停止します
val handle = plugin.playEffect(effect, every = 1.ticks) { projectile.takeIf { it.isValid }?.location }
handle.cancel()
```

## アニメーション

`animatedEffect` はフレームごとにエフェクトを作り直し、フレーム番号を渡します。回転や拡大する形状はこれで作ります。

```kotlin
val spinner = animatedEffect { frame ->
  particle(Particle.FLAME, circle(1.0, points = 6).rotateY(frame * 0.3))
  particle(Particle.SOUL_FIRE_FLAME, helix(0.5, 2.0).rotateY(-frame * 0.2))
}
plugin.playEffect(spinner, player, every = 1.ticks, duration = 5.seconds)
```

動かないエフェクトは `effect { }` を使ってください。形状は一度だけ計算されます。`animatedEffect` は毎フレーム計算し直します。

## 文字・画像のパーティクルアート

`textShape` はJava AWTフォントを画像化し、`imageShape` は透明でない画素を点へ変換します。
通常の `Shape` を返すため、移動・回転・拡大・結合とすべての再生APIをそのまま使えます。

```kotlin
val text = textShape("DevCore", scale = 0.06, sampleStep = 3, maxPoints = 300)
val logo = imageShape(File("logo.png"), width = 32, pixelSize = 0.05,
  sampleStep = 2, alphaThreshold = 32, maxPoints = 500)
effect { particle(Particle.DUST, text, data = dust(Color.AQUA)) }
  .play(player.location.add(0.0, 2.5, 0.0), listOf(player))
```

負荷制限として `sampleStep` と `maxPoints` を必ず調整してください。画像は縦横比を維持し、
透明画素を除外します。ディスプレイのないヘッドレスサーバーJVMでも動作します。

## 補足

- `particle(count = 0, offset = ...)` では、Bukkit API と同じく `offset` を方向として扱います。
- `Effect` は `fun interface` なので、`Effect { location, audience, frame -> ... }` と自作もできます。
- 以前の `Effects.spawnCloud(location)` も引き続き使えます。
