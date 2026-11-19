# DevCore Effect

[English] | [[日本語](README.ja.md)]

Reusable particle and sound effects, shape generators, and repeating playback that can follow an entity.
No more hand-written `sin`/`cos` loops and `BukkitRunnable` counters.

## Install (Gradle Kotlin DSL)

```kotlin
dependencies {
  implementation("com.peco2282.devcore:effect:<version>")
}
```

Included in `core`. Depends on `scheduler` (exposed as `api`, because playback uses `Ticks` and `TaskHandle`).

## Quick start

```kotlin
// Build once, keep it in a field, play it whenever you want.
val levelUp = effect {
  particle(Particle.END_ROD, circle(1.5).offset(y = 1.0))
  particle(Particle.DUST, sphere(0.6).offset(y = 1.0), data = dust(Color.AQUA, 1.2f))
  sound(Sound.ENTITY_PLAYER_LEVELUP)
}

levelUp.play(player.location)                    // once, for everyone nearby
levelUp.play(player.location, listOf(player))    // once, only for these players
```

## Shapes

A `Shape` is plain geometry (offsets from an origin). It does not touch the server, so it is easy to test.

| Function | Result |
|---|---|
| `point()` | A single point at the origin |
| `line(to, density = 0.25)` | A line from the origin to `to`; `density` is the gap between points |
| `circle(radius, points = 36, normal = UP, arc = 2π, start = 0.0)` | A circle or arc in the plane perpendicular to `normal` |
| `sphere(radius, points = 100)` | Evenly distributed points on a sphere |
| `helix(radius, height, turns = 3.0, points = 60)` | A helix rising along +Y |

Shapes can be transformed and combined; every operation returns a new shape:

```kotlin
circle(1.0).rotateX(PI / 4).offset(y = 1.0)   // tilted ring at head height
helix(0.5, 2.0) + circle(1.0)                  // combine two shapes
shape.scale(2.0).rotateY(angle)
```

## Playing repeatedly

`playEffect` runs an effect every `every` ticks, for `duration` (or until you cancel the returned handle).

```kotlin
// At a fixed location
plugin.playEffect(effect, location, every = 2.ticks, duration = 5.seconds)

// Following an entity; stops by itself when the entity is removed / the player logs out
plugin.playEffect(effect, player, every = 2.ticks, duration = 5.seconds)

// Any moving point. Returning null stops playback.
val handle = plugin.playEffect(effect, every = 1.ticks) { projectile.takeIf { it.isValid }?.location }
handle.cancel()
```

## Animated effects

`animatedEffect` rebuilds the effect for each frame and passes the frame number, which is how you make spinning or growing shapes.

```kotlin
val spinner = animatedEffect { frame ->
  particle(Particle.FLAME, circle(1.0, points = 6).rotateY(frame * 0.3))
  particle(Particle.SOUL_FIRE_FLAME, helix(0.5, 2.0).rotateY(-frame * 0.2))
}
plugin.playEffect(spinner, player, every = 1.ticks, duration = 5.seconds)
```

Prefer `effect { }` for static effects: its shapes are computed once, while `animatedEffect` recomputes them every frame.

## Text and image particle art

`textShape` rasterizes a Java AWT font and `imageShape` samples non-transparent image pixels. Both
produce ordinary shapes, so every transform and playback API above remains available.

```kotlin
val text = textShape("DevCore", scale = 0.06, sampleStep = 3, maxPoints = 300)
val logo = imageShape(File("logo.png"), width = 32, pixelSize = 0.05,
  sampleStep = 2, alphaThreshold = 32, maxPoints = 500)

val art = effect { particle(Particle.DUST, text, data = dust(Color.AQUA)) }
art.play(player.location.add(0.0, 2.5, 0.0), listOf(player))
```

Use `sampleStep` and `maxPoints` as hard performance controls. Images preserve aspect ratio;
transparent pixels are skipped. Rendering is compatible with headless server JVMs.

## Notes

- `particle(count = 0, offset = ...)` treats `offset` as a direction, as in the Bukkit API.
- `Effect` is a `fun interface`, so you can also write your own: `Effect { location, audience, frame -> ... }`.
- `Effects.spawnCloud(location)` from earlier versions is still available.
