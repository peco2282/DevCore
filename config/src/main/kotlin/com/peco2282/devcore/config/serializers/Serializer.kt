package com.peco2282.devcore.config.serializers

interface Serializer<T : Any> {

  fun deserialize(value: Any?): T

  fun serialize(value: T): Any?
}
