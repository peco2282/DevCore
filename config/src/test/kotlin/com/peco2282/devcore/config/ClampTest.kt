package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import com.peco2282.devcore.config.validations.annotations.Clamp
import com.peco2282.devcore.config.validations.annotations.ClampAtLeast
import com.peco2282.devcore.config.validations.annotations.ClampAtMost
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClampTest {

  data class Limits(
    @Clamp(min = 0.0, max = 1.0)
    val ratio: Double = 0.5,

    @ClampAtLeast(1.0)
    val workers: Int = 1,

    @ClampAtMost(60.0)
    val timeout: Long = 30
  )

  @Test
  fun `values are clamped and written back`() {
    val yaml = YamlConfiguration().apply {
      set("ratio", 1.5)
      set("workers", -2)
      set("timeout", 120)
    }

    val config = ClassMapper.create(Limits::class, yaml)

    assertEquals(1.0, config.ratio)
    assertEquals(1, config.workers)
    assertEquals(60L, config.timeout)
    assertEquals(1.0, yaml.getDouble("ratio"))
    assertEquals(1, yaml.getInt("workers"))
    assertEquals(60L, yaml.getLong("timeout"))
  }

  data class IntegerBounds(
    @Clamp(min = 0.5, max = 3.5)
    val count: Int = 2
  )

  @Test
  fun `fractional bounds are adjusted to the integer domain`() {
    val below = YamlConfiguration().apply { set("count", 0) }
    val above = YamlConfiguration().apply { set("count", 5) }

    assertEquals(1, ClassMapper.create(IntegerBounds::class, below).count)
    assertEquals(3, ClassMapper.create(IntegerBounds::class, above).count)
  }

  data class NestedConfig(
    val limits: Limits = Limits()
  )

  @Test
  fun `clamp annotations apply in nested sections`() {
    val yaml = YamlConfiguration().apply {
      set("limits.ratio", -1.0)
    }

    val config = ClassMapper.create(NestedConfig::class, yaml)

    assertEquals(0.0, config.limits.ratio)
    assertEquals(0.0, yaml.getDouble("limits.ratio"))
  }

  data class Entry(
    @ClampAtLeast(0.0)
    val priority: Int = 0
  )

  data class ListConfig(
    val entries: List<Entry> = emptyList()
  )

  @Test
  fun `clamp annotations apply in data class lists`() {
    val yaml = YamlConfiguration().apply {
      set("entries", listOf(mapOf("priority" to -10)))
    }

    val config = ClassMapper.create(ListConfig::class, yaml)

    assertEquals(0, config.entries.single().priority)
    @Suppress("UNCHECKED_CAST")
    val saved = yaml.getList("entries")!!.single() as Map<String, Any?>
    assertEquals(0, saved["priority"])
  }

  data class InvalidBounds(
    @Clamp(min = 10.0, max = 1.0)
    val value: Double = 5.0
  )

  @Test
  fun `invalid clamp bounds are rejected`() {
    val yaml = YamlConfiguration().apply { set("value", 5.0) }

    assertThrows<IllegalArgumentException> {
      ClassMapper.create(InvalidBounds::class, yaml)
    }
  }
}
