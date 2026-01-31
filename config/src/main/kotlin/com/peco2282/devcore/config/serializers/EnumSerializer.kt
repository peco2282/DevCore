package com.peco2282.devcore.config.serializers

import kotlin.reflect.KClass

class EnumSerializer<T : Enum<T>>(
  private val type: KClass<T>
) : Serializer<T> {

  override fun deserialize(value: Any?): T {
    return java.lang.Enum.valueOf(type.java, value.toString())
  }

  override fun serialize(value: T): Any {
    return value.name
  }
}

