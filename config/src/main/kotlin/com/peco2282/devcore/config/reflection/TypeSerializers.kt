package com.peco2282.devcore.config.reflection

import com.peco2282.devcore.config.serializers.Serializer
import kotlin.reflect.KClass

object TypeSerializers {

  private val serializers = mutableMapOf<KClass<*>, Serializer<*>>()

  fun <T : Any> register(type: KClass<T>, serializer: Serializer<T>) {
    serializers[type] = serializer
  }

  fun has(type: KClass<*>) = serializers.containsKey(type)

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> deserialize(type: KClass<T>, value: Any?): T {
    return (serializers[type] as Serializer<T>).deserialize(value)
  }

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> get(kClass: KClass<T>): Serializer<T>? {
    return serializers[kClass] as? Serializer<T>
  }

  fun deserializeOrRaw(type: KClass<*>, value: Any?): Any? {
    return if (has(type)) {
      deserialize(type as KClass<Any>, value)
    } else {
      value
    }
  }

}

