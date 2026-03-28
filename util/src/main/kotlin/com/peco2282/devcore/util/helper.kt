package com.peco2282.devcore.util

inline fun <T, reified E: T> T.asNullable(orThrow: Boolean = false): E? = (this as? E).also { if (orThrow && it == null) throw IllegalArgumentException("Cannot cast to non-null type ${E::class.qualifiedName}") }

inline fun <T, reified E: T> T.asNotNullable(): E = this as E
