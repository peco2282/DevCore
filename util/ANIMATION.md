# Easing and Tween

`Easing` is a pure `(Double) -> Double`, and `Tween` is a deterministic inclusive timeline.

```kotlin
Easing.OUT_CUBIC(0.5)

val animation = tween(
  from = 0.0,
  to = 1.0,
  duration = 2.seconds,
  easing = Easing.IN_OUT_SINE
)

animation.frames().forEach(::println) // deterministic/offline sampling
val task = animation.start(plugin, periodTicks = 1) { value ->
  bossBar.progress(value.toFloat())
}
task.cancel() // optional; Bukkit also cancels it when the plugin disables
```

Inputs outside the easing range are clamped. Back easings intentionally overshoot; sine and cubic easings stay inside `0.0..1.0`.
