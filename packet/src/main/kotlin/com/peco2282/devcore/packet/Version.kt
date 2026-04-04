package com.peco2282.devcore.packet

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
  fun packageString(): String = "v${major}_${minor}_$patch"

  override fun toString(): String = "$major.$minor.$patch"
  override fun equals(other: Any?): Boolean = other is Version && compareTo(other) == 0
  override fun hashCode(): Int = toString().hashCode()

  operator fun contains(version: Version): Boolean = compareTo(version) >= 0
  
  companion object {
    fun parse(version: String): Version = Version(version)
  }
}

operator fun String.rangeTo(other: String): ClosedRange<Version> = Version(this)..Version(other)

operator fun String.rangeUntil(other: String): OpenEndRange<Version> = Version(this)..<Version(other)

operator fun ClosedRange<Version>.contains(version: String): Boolean = Version(version) in this

operator fun OpenEndRange<Version>.contains(version: String): Boolean = Version(version) in this
