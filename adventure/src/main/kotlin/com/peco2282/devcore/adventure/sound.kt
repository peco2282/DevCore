package com.peco2282.devcore.adventure

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component

class SoundBuilder internal constructor(private val name: String) {
  var source: Sound.Source = Sound.Source.MASTER
  var volume: Float = 1f
  var pitch: Float = 1f
  internal fun build(): Sound {
    require(volume >= 0f && volume.isFinite()) { "Volume must be finite and non-negative" }
    require(pitch >= 0f && pitch.isFinite()) { "Pitch must be finite and non-negative" }
    return Sound.sound(Key.key(name), source, volume, pitch)
  }
}

fun sound(name: String, block: SoundBuilder.() -> Unit = {}): Sound = SoundBuilder(name).apply(block).build()
fun Audience.playSound(name: String, block: SoundBuilder.() -> Unit = {}) = playSound(sound(name, block))
fun Audience.sendMessageWithSound(message: Component, sound: Sound) { sendMessage(message); playSound(sound) }
