package com.peco2282.devcore.pdc

import org.bukkit.persistence.PersistentDataType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Resolves the [PersistentDataType] to use for a Kotlin type.
 *
 * Built in: `String`, `Int`, `Long`, `Double`, `Float`, `Short`, `Byte`, `Boolean`, `ByteArray`,
 * `IntArray`, `LongArray`, [UUID], any `enum`, and `List<T>` of the primitive types above.
 * Register your own with [register] or [registerString].
 */
object PdcTypes {
  private val custom = ConcurrentHashMap<KClass<*>, PersistentDataType<*, *>>()

  /** Registers [type] as the way to store values of [kClass]. */
  fun <T : Any> register(kClass: KClass<T>, type: PersistentDataType<*, T>) {
    custom[kClass] = type
  }

  /**
   * Registers a type that is stored as a string, e.g. an id or a value that already has a text form.
   *
   * ```kotlin
   * PdcTypes.registerString(Color::class, { it.asRGB().toString() }, { Color.fromRGB(it.toInt()) })
   * ```
   */
  fun <T : Any> registerString(kClass: KClass<T>, encode: (T) -> String, decode: (String) -> T) {
    register(kClass, StringBacked(kClass.java, encode, decode))
  }

  /** The type for [T]. @throws IllegalArgumentException if [T] is not supported. */
  inline fun <reified T : Any> of(): PersistentDataType<*, T> = resolve(typeOf<T>())

  /** The type for [kType]. @throws IllegalArgumentException if it is not supported. */
  @Suppress("UNCHECKED_CAST")
  fun <T : Any> resolve(kType: KType): PersistentDataType<*, T> {
    val kClass = kType.classifier as? KClass<*>
      ?: throw IllegalArgumentException("Unsupported type: $kType")
    custom[kClass]?.let { return it as PersistentDataType<*, T> }
    val builtIn: PersistentDataType<*, *>? = when (kClass) {
      String::class -> PersistentDataType.STRING
      Int::class -> PersistentDataType.INTEGER
      Long::class -> PersistentDataType.LONG
      Double::class -> PersistentDataType.DOUBLE
      Float::class -> PersistentDataType.FLOAT
      Short::class -> PersistentDataType.SHORT
      Byte::class -> PersistentDataType.BYTE
      Boolean::class -> PersistentDataType.BOOLEAN
      ByteArray::class -> PersistentDataType.BYTE_ARRAY
      IntArray::class -> PersistentDataType.INTEGER_ARRAY
      LongArray::class -> PersistentDataType.LONG_ARRAY
      UUID::class -> StringBacked(UUID::class.java, UUID::toString, UUID::fromString)
      List::class -> listOf(kType)
      else -> null
    }
    if (builtIn != null) return builtIn as PersistentDataType<*, T>
    if (kClass.java.isEnum) return enumType(kClass.java) as PersistentDataType<*, T>
    throw IllegalArgumentException(
      "No PersistentDataType for $kType. Register one with PdcTypes.register / registerString."
    )
  }

  private fun listOf(kType: KType): PersistentDataType<*, *> {
    val element = kType.arguments.singleOrNull()?.type?.classifier
    val lists = PersistentDataType.LIST
    return when (element) {
      String::class -> lists.strings()
      Int::class -> lists.integers()
      Long::class -> lists.longs()
      Double::class -> lists.doubles()
      Float::class -> lists.floats()
      Short::class -> lists.shorts()
      Byte::class -> lists.bytes()
      Boolean::class -> lists.booleans()
      else -> throw IllegalArgumentException("Unsupported list element type in $kType")
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun enumType(cls: Class<*>): PersistentDataType<*, *> {
    val constants = cls.enumConstants as Array<Enum<*>>
    return StringBacked(
      cls as Class<Enum<*>>,
      { it.name },
      { name -> constants.firstOrNull { it.name == name } ?: throw IllegalArgumentException("Unknown ${cls.simpleName}: $name") }
    )
  }
}

/** A value stored as its string form. */
private class StringBacked<T : Any>(
  private val type: Class<T>,
  private val encode: (T) -> String,
  private val decode: (String) -> T
) : PersistentDataType<String, T> {
  override fun getPrimitiveType(): Class<String> = String::class.java
  override fun getComplexType(): Class<T> = type
  override fun toPrimitive(complex: T, context: org.bukkit.persistence.PersistentDataAdapterContext): String = encode(complex)
  override fun fromPrimitive(primitive: String, context: org.bukkit.persistence.PersistentDataAdapterContext): T = decode(primitive)
}
