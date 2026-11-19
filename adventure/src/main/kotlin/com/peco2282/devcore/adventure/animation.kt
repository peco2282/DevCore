package com.peco2282.devcore.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

fun interface TextAnimation {
  fun frame(index: Int): Component

  companion object {
    fun typewriter(component: Component): FiniteTextAnimation {
      val text = PlainTextComponentSerializer.plainText().serialize(component)
      return FiniteTextAnimation(text.length.coerceAtLeast(1)) { index -> Component.text(text.take((index + 1).coerceAtMost(text.length))).style(component.style()) }
    }

    fun scroll(component: Component, width: Int, padding: Int = 3): FiniteTextAnimation {
      require(width > 0 && padding >= 0)
      val text = PlainTextComponentSerializer.plainText().serialize(component)
      val loop = text + " ".repeat(padding)
      return FiniteTextAnimation(loop.length.coerceAtLeast(1)) { index ->
        Component.text((loop + loop).drop(index % loop.length).take(width)).style(component.style())
      }
    }

    fun blink(component: Component, on: Int = 10, off: Int = 10): FiniteTextAnimation {
      require(on > 0 && off > 0)
      return FiniteTextAnimation(on + off) { index -> if (index % (on + off) < on) component else Component.empty() }
    }

    fun gradientSweep(text: String, vararg colors: TextColor): FiniteTextAnimation {
      require(colors.size >= 2)
      return FiniteTextAnimation(text.length.coerceAtLeast(1)) { frame ->
        component { text(text) { gradient { colors(*colors); phase(frame.toFloat() / text.length.coerceAtLeast(1)) } } }
      }
    }
  }
}

class FiniteTextAnimation(val length: Int, private val renderer: (Int) -> Component) : TextAnimation {
  init { require(length > 0) }
  override fun frame(index: Int): Component = renderer(Math.floorMod(index, length))
}

fun progressAnimation(from: Double, to: Double, frames: Int, easing: (Double) -> Double = { it }): (Int) -> Double {
  require(frames > 0)
  return { frame ->
    val progress = if (frames == 1) 1.0 else frame.coerceIn(0, frames - 1).toDouble() / (frames - 1)
    from + (to - from) * easing(progress)
  }
}
