package com.peco2282.devcore.adventure.builder

import net.kyori.adventure.text.format.TextColor

interface Gradient {
  fun gradient(vararg colors: TextColor): Gradient
}