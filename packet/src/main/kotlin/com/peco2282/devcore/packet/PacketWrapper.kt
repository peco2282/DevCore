package com.peco2282.devcore.packet

import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

/**
 * Wraps a raw NMS packet object, providing access to the original instance.
 *
 * Implement this interface in version-specific modules to expose NMS packets
 * through the common API without introducing direct NMS dependencies.
 */
interface PacketWrapper {
  /** The original NMS packet instance. */
  val original: Any
}

// The common API treats NMS packets as Any to avoid direct compile-time dependencies.
// Cast and use the concrete type on the version-specific submodule side.

private val fieldCache = ConcurrentHashMap<Class<*>, Map<String, Field>>()

/**
 * Returns the value of the field named [fieldName] from this object using reflection.
 *
 * Field lookups are cached per class for performance.
 *
 * @param T The expected type of the field value.
 * @param fieldName The name of the field to read.
 * @throws NoSuchFieldException if the field does not exist on this object's class.
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
 * Sets the value of the field named [fieldName] on this object using reflection.
 *
 * Field lookups are cached per class for performance.
 *
 * @param fieldName The name of the field to write.
 * @param value The new value to assign.
 * @throws NoSuchFieldException if the field does not exist on this object's class.
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
 * Convenience inline helper for reading a typed field from a packet object.
 *
 * Equivalent to calling [getFieldValue] with a reified type parameter.
 *
 * @param T The expected type of the field value.
 * @param fieldName The name of the field to read.
 */
inline fun <reified T> Any.packetField(fieldName: String): T = getFieldValue(fieldName)