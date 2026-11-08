package com.peco2282.devcore.util

import org.bukkit.Bukkit

/**
 * A Minecraft server version, compared numerically (`1.21.10` is newer than `1.21.4`).
 *
 * Plain string comparison gets this wrong: `"1.21.10" < "1.21.4"`. Always compare versions
 * through this class.
 */
class MinecraftVersion(val major: Int, val minor: Int, val patch: Int = 0) : Comparable<MinecraftVersion> {
  override fun compareTo(other: MinecraftVersion): Int =
    compareValuesBy(this, other, MinecraftVersion::major, MinecraftVersion::minor, MinecraftVersion::patch)

  override fun equals(other: Any?): Boolean = other is MinecraftVersion && compareTo(other) == 0
  override fun hashCode(): Int = 31 * (31 * major + minor) + patch
  override fun toString(): String = "$major.$minor.$patch"

  companion object {
    /**
     * Parses `1.21`, `1.21.4`, or strings with a suffix such as `1.21.4-rc1`.
     *
     * @throws IllegalArgumentException if [version] has no numeric major and minor part.
     */
    fun parse(version: String): MinecraftVersion {
      val parts = version.trim().split(".")
      require(parts.size >= 2) { "Invalid version format: $version" }
      fun number(text: String) = text.takeWhile { it.isDigit() }.toIntOrNull()
      val major = number(parts[0]) ?: throw IllegalArgumentException("Invalid major version: $version")
      val minor = number(parts[1]) ?: throw IllegalArgumentException("Invalid minor version: $version")
      val patch = parts.getOrNull(2)?.let(::number) ?: 0
      return MinecraftVersion(major, minor, patch)
    }

    /** The version of the running server. */
    fun current(): MinecraftVersion = parse(Bukkit.getMinecraftVersion())
  }
}

/**
 * Maps Minecraft versions to the class that implements a feature for them, then loads it.
 *
 * This is the one place version dispatch should happen for modules that keep per-version
 * implementations in `v1_XX_X` submodules. Each entry says "from this version on, use this class"
 * until the next entry takes over.
 *
 * ```kotlin
 * val provider = versioned<MyProvider>("my feature") {
 *   since("1.20.6", "com.example.v1_20_6.Impl")
 *   since("1.21.4", "com.example.v1_21_4.Impl")
 *   supportedBefore("1.22")
 * }.load()
 * ```
 */
class VersionedImpls<T : Any> @PublishedApi internal constructor(
  private val feature: String,
  private val type: Class<T>
) {
  private val entries = mutableListOf<Pair<MinecraftVersion, String>>()
  private var upperBound: MinecraftVersion? = null

  /** Use [className] from [version] up to the next [since] entry. */
  fun since(version: String, className: String) {
    entries += MinecraftVersion.parse(version) to className
    entries.sortBy { it.first }
  }

  /** Versions at or above [version] are reported as unsupported. Without it, newer versions use the latest entry. */
  fun supportedBefore(version: String) {
    upperBound = MinecraftVersion.parse(version)
  }

  /** The class name for [version], or `null` if the version is too old or too new. */
  fun classNameFor(version: MinecraftVersion): String? {
    if (upperBound?.let { version >= it } == true) return null
    return entries.lastOrNull { it.first <= version }?.second
  }

  /**
   * Instantiates the implementation for [version].
   *
   * @throws IllegalStateException with the supported range when no implementation matches,
   *   or when the class cannot be loaded (missing submodule in the jar).
   */
  fun load(version: MinecraftVersion = MinecraftVersion.current()): T {
    val className = classNameFor(version) ?: throw IllegalStateException(
      "$feature is not supported on Minecraft $version " +
        "(supported: ${entries.firstOrNull()?.first ?: "none"} up to ${upperBound?.let { "before $it" } ?: "latest"})"
    )
    return try {
      type.cast(Class.forName(className, true, type.classLoader ?: javaClass.classLoader).getDeclaredConstructor().newInstance())
    } catch (e: ReflectiveOperationException) {
      throw IllegalStateException("Failed to load $feature implementation $className for Minecraft $version", e)
    }
  }
}

/** Declares the per-version implementations of [feature]. See [VersionedImpls]. */
inline fun <reified T : Any> versioned(feature: String, block: VersionedImpls<T>.() -> Unit): VersionedImpls<T> =
  VersionedImpls(feature, T::class.java).apply(block)
