package com.peco2282.devcore.packet

/**
 * Represents a Minecraft server version in the format `major.minor.patch`.
 *
 * Supports comparison, range checks, and conversion to package-style strings.
 *
 * @property major The major version number (e.g. `1`).
 * @property minor The minor version number (e.g. `20`).
 * @property patch The patch version number (e.g. `4`). Defaults to `0` if omitted.
 */
class Version: Comparable<Version> {
  val major: Int
  val minor: Int
  val patch: Int
  constructor(version: String) {
    val parts = version.split(".")
    if (parts.size < 2) {
      throw IllegalArgumentException("Invalid version format: $version")
    }
    this.major = parts[0].toIntOrNull() ?: throw IllegalArgumentException("Invalid major version: ${parts[0]}")
    this.minor = parts[1].toIntOrNull() ?: throw IllegalArgumentException("Invalid minor version: ${parts[1]}")
    this.patch = if (parts.size > 2) parts[2].toIntOrNull() ?: 0 else 0
  }

  override fun compareTo(other: Version): Int = compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

  /**
   * Returns the version as a package-style string (e.g. `v1_20_4`).
   */
  fun packageString(): String = "v${major}_${minor}_$patch"

  override fun toString(): String = "$major.$minor.$patch"
  override fun equals(other: Any?): Boolean = other is Version && compareTo(other) == 0
  override fun hashCode(): Int = toString().hashCode()

  /**
   * Returns `true` if this version is greater than or equal to [version].
   */
  operator fun contains(version: Version): Boolean = compareTo(version) >= 0

  companion object {
    /**
     * Parses a version string and returns a [Version] instance.
     */
    fun parse(version: String): Version = Version(version)
  }
}

/** Creates a closed [Version] range from two version strings. */
operator fun String.rangeTo(other: String): ClosedRange<Version> = Version(this)..Version(other)

/** Creates an open-ended [Version] range from two version strings. */
operator fun String.rangeUntil(other: String): OpenEndRange<Version> = Version(this)..<Version(other)

/** Returns `true` if the version string falls within this closed range. */
operator fun ClosedRange<Version>.contains(version: String): Boolean = Version(version) in this

/** Returns `true` if the version string falls within this open-ended range. */
operator fun OpenEndRange<Version>.contains(version: String): Boolean = Version(version) in this
