@file:JvmMultifileClass
@file:JvmName("HelperKt")

package com.peco2282.devcore.util

/**
 * Functional interface for operations that may throw exceptions.
 *
 * Useful for wrapping code that might fail into a callable interface,
 * often used with [getOrDefault] for safe execution with fallback values.
 *
 * @param T The type of value returned by the getter.
 */
fun interface ThrowableGetter<T> {
  @Throws(Throwable::class)
  fun get(): T
}

/**
 * Executes a [ThrowableGetter] and returns its result, or a default value if it throws an exception.
 *
 * @param T The type of value to return.
 * @param default The default value to return if [getter] throws an exception.
 * @param getter The [ThrowableGetter] to execute.
 * @return The result of [getter.get()] if successful, otherwise [default].
 *
 * @sample
 * ```kotlin
 * val config = getOrDefault("default.yml") {
 *     File("config.yml").readText()
 * }
 * ```
 */
fun <T> getOrDefault(default: T, getter: ThrowableGetter<T>): T = try {
  getter.get()
} catch (e: Throwable) {
  default
}
