package com.peco2282.devcore.adventure

internal fun <T> MutableList<T>.updateLast(transformer: (T) -> T) {
  if (isEmpty()) return
  this[lastIndex] = transformer(this[lastIndex])
}
