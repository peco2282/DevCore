package com.peco2282.devcore.config.reflection

import com.peco2282.devcore.config.serializers.AdventureSerializers
import com.peco2282.devcore.config.serializers.BukkitSerializers
import com.peco2282.devcore.config.serializers.Serializer
import kotlin.reflect.KClass

/**
 * Registry for type serializers.
 *
 * This singleton manages a mapping between classes and their corresponding [Serializer]s,
 * allowing for custom serialization and deserialization logic in the configuration system.
 */
object TypeSerializers {

  private val serializers = mutableMapOf<KClass<*>, Serializer<*>>()

  private var initialized = false

  private fun ensureInitialized() {
    if (initialized) return
    initialized = true
    AdventureSerializers.registerAll()
    BukkitSerializers.registerAll()
  }

  /**
   * Registers a [serializer] for the specified [type].
   *
   * @param T the type to register a serializer for
   * @param type the [KClass] of type [T]
   * @param serializer the [Serializer] instance to handle type [T]
   */
  fun <T : Any> register(type: KClass<T>, serializer: Serializer<T>) {
    serializers[type] = serializer
  }

  /**
   * Returns whether a serializer is registered for the specified [type].
   *
   * @param type the [KClass] to check
   * @return true if a serializer is registered, false otherwise
   */
  fun has(type: KClass<*>): Boolean {
    ensureInitialized()
    return serializers.containsKey(type)
  }

  /**
   * Deserializes the [value] to type [T] using the registered serializer.
   *
   * @param T the target type
   * @param type the [KClass] of type [T]
   * @param value the raw value to deserialize
   * @return the deserialized instance of type [T]
   * @throws NoSuchElementException if no serializer is registered for [type]
   * @throws ClassCastException if the registered serializer is not for type [T]
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> deserialize(type: KClass<T>, value: Any?): T {
    ensureInitialized()
    return (serializers[type] as Serializer<T>).deserialize(value)
  }

  /**
   * Gets the registered serializer for the specified [kClass].
   *
   * @param T the type of the serializer
   * @param kClass the [KClass] to get the serializer for
   * @return the [Serializer] instance, or null if none is registered
   */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> get(kClass: KClass<T>): Serializer<T>? {
    ensureInitialized()
    val serializer = serializers[kClass]
    if (serializer != null) return serializer as? Serializer<T>

    // 継承関係を遡って検索
    for (entry in serializers) {
      if (entry.key.java.isAssignableFrom(kClass.java)) {
        return entry.value as? Serializer<T>
      }
    }

    return null
  }

  /**
   * Deserializes the [value] to type [T] if a serializer is registered, otherwise returns the raw [value].
   *
   * @param type the [KClass] of the expected type
   * @param value the raw value to deserialize
   * @return the deserialized value if a serializer exists, otherwise the raw [value]
   */
  fun deserializeOrRaw(type: KClass<*>, value: Any?): Any? {
    val serializer = get(type as KClass<Any>)
    return serializer?.deserialize(value) ?: value
  }

  /**
   * Serializes the [value] if a serializer is registered, otherwise returns the raw [value].
   *
   * @param value the value to serialize
   * @return the serialized value if a serializer exists, otherwise the raw [value]
   */
  fun serializeOrRaw(value: Any): Any? {
    val type = value::class as KClass<Any>
    val serializer = get(type)
    if (serializer != null) return serializer.serialize(value)

    return when {
      value is Enum<*> -> value.name
      else -> value
    }
  }

}

