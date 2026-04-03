package com.peco2282.devcore.packet

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

interface PacketWrapper {
  val original: Any
}

// Eliminate direct dependency on Minecraft's Packet class from the common API and treat it as Any.
// Cast and use as needed on the submodule side.

private val fieldCache = ConcurrentHashMap<Class<*>, Map<String, Field>>()

/**
 * Gets the field value using reflection.
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
 * Sets the field value using reflection.
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
 * Helper for safely getting a field from a packet.
 */
inline fun <reified T> Any.packetField(fieldName: String): T = getFieldValue(fieldName)