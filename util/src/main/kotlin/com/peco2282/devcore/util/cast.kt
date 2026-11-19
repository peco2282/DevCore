@file:JvmMultifileClass
@file:JvmName("HelperKt")

package com.peco2282.devcore.util

/**
 * Safely casts this object to the specified type [E], returning null if the cast fails.
 *
 * @param E The target type to cast to. Must be a subtype of [T].
 * @param orThrow If true, throws [IllegalArgumentException] when cast fails; if false, returns null.
 * @return The casted object of type [E], or null if cast fails and [orThrow] is false.
 * @throws IllegalArgumentException if [orThrow] is true and the cast fails.
 *
 * @sample
 * ```kotlin
 * val obj: Any = "Hello"
 * val str: String? = obj.asNullable<Any, String>() // Returns "Hello"
 * val num: Int? = obj.asNullable<Any, Int>() // Returns null
 * val forced: Int? = obj.asNullable<Any, Int>(orThrow = true) // Throws IllegalArgumentException
 * ```
 */
inline fun <T, reified E: T> T.asNullable(orThrow: Boolean = false): E? = (this as? E).also { if (orThrow && it == null) throw IllegalArgumentException("Cannot cast to non-null type ${E::class.qualifiedName}") }

/**
 * Casts this object to the specified type [E] without null safety checks.
 *
 * @param E The target type to cast to. Must be a subtype of [T].
 * @return The casted object of type [E].
 * @throws ClassCastException if the cast is invalid.
 *
 * @sample
 * ```kotlin
 * val obj: Any = "Hello"
 * val str: String = obj.asNotNullable<Any, String>() // Returns "Hello"
 * ```
 */
inline fun <T, reified E: T> T.asNotNullable(): E = this as E
