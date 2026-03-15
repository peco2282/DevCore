package com.peco2282.devcore.packet

import net.minecraft.network.protocol.Packet

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

interface PacketWrapper {
  val original: Any
}

fun PacketWrapper.getOriginalPacket(): Packet<*> {
  return original as Packet<*>
}

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