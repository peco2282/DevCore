package com.peco2282.devcore.packet

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

interface PacketWrapper {
  val original: Any
}

// 共通 API からは Minecraft の Packet クラスへの直接依存を排除し、Any として扱う。
// サブモジュール側で必要に応じてキャストして使用する。

private val fieldCache = ConcurrentHashMap<Class<*>, Map<String, Field>>()

/**
 * リフレクションを使用してフィールドの値を取得します。
 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getFieldValue(fieldName: String): T {
  val clazz = this::class.java
  val fields = fieldCache.getOrPut(clazz) {
    clazz.declaredFields.associateBy { it.name }
  }
  val field = fields[fieldName] ?: throw NoSuchFieldException("Field $fieldName not found in ${clazz.name}")
  field.isAccessible = true
  return field.get(this) as T
}

/**
 * リフレクションを使用してフィールドの値を設定します。
 */
fun Any.setFieldValue(fieldName: String, value: Any?) {
  val clazz = this::class.java
  val fields = fieldCache.getOrPut(clazz) {
    clazz.declaredFields.associateBy { it.name }
  }
  val field = fields[fieldName] ?: throw NoSuchFieldException("Field $fieldName not found in ${clazz.name}")
  field.isAccessible = true
  field.set(this, value)
}

/**
 * パケットからフィールドを型安全に取得するためのヘルパー。
 */
inline fun <reified T> Any.packetField(fieldName: String): T = getFieldValue(fieldName)