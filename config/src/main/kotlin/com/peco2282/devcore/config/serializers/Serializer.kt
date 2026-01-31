package com.peco2282.devcore.config.serializers

/**
 * Interface for serializing and deserializing values of type [T].
 *
 * Implementations of this interface define how to convert between Kotlin objects
 * and their representations in configuration files.
 *
 * @param T the type of the object being serialized
 */
interface Serializer<T : Any> {

  /**
   * Deserializes the [value] from its configuration format back to type [T].
   *
   * @param value the raw value from the configuration
   * @return the deserialized object of type [T]
   */
  fun deserialize(value: Any?): T

  /**
   * Serializes the [value] of type [T] to a format suitable for the configuration.
   *
   * @param value the object to serialize
   * @return the serialized representation of the [value]
   */
  fun serialize(value: T): Any?
}
