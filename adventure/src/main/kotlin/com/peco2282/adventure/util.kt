package com.peco2282.adventure

internal fun <T> MutableList<T>.updateLast(transformer: (T) -> T) {
  if (isEmpty()) return
  this[lastIndex] = transformer(this[lastIndex])
}
