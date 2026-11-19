package com.peco2282.devcore.util.animation

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** Common easing curves. Inputs are clamped to `0.0..1.0`. */
enum class Easing(private val curve: (Double) -> Double) : (Double) -> Double {
  LINEAR({ it }),
  IN_SINE({ 1.0 - cos(it * PI / 2.0) }),
  OUT_SINE({ sin(it * PI / 2.0) }),
  IN_OUT_SINE({ -(cos(PI * it) - 1.0) / 2.0 }),
  IN_CUBIC({ it * it * it }),
  OUT_CUBIC({ 1.0 - (1.0 - it).pow(3) }),
  IN_OUT_CUBIC({ if (it < 0.5) 4.0 * it * it * it else 1.0 - (-2.0 * it + 2.0).pow(3) / 2.0 }),
  IN_BACK({ x -> val c = 1.70158; (c + 1.0) * x * x * x - c * x * x }),
  OUT_BACK({ x -> val c = 1.70158; 1.0 + (c + 1.0) * (x - 1.0).pow(3) + c * (x - 1.0).pow(2) });

  override operator fun invoke(progress: Double): Double = curve(progress.coerceIn(0.0, 1.0))
}
