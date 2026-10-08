package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import com.peco2282.devcore.config.validations.annotations.ConfigIgnore
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.util.Vector
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ConfigIgnoreTest {

  data class OffsetConfig(
    val x: Double = 1.0,
    val z: Double = 1.0,
    @ConfigIgnore val runtimeLabel: String = "runtime"
  ) {
    @ConfigIgnore
    val vector: Vector
      get() = Vector(x, 0.0, z)
  }

  @Test
  fun `ignored constructor and derived properties are not written`() {
    val yaml = YamlConfiguration()

    val config = ClassMapper.create(OffsetConfig::class, yaml)

    assertEquals("runtime", config.runtimeLabel)
    assertEquals(Vector(1.0, 0.0, 1.0), config.vector)
    assertFalse(yaml.contains("runtimeLabel"))
    assertFalse(yaml.contains("vector"))
  }

  @Test
  fun `ignored constructor property is not read`() {
    val yaml = YamlConfiguration().apply {
      set("runtimeLabel", "from-yaml")
    }

    val config = ClassMapper.create(OffsetConfig::class, yaml)

    assertEquals("runtime", config.runtimeLabel)
    assertEquals("from-yaml", yaml.getString("runtimeLabel"))
  }

  data class ContainerConfig(
    val offsets: List<OffsetConfig> = listOf(OffsetConfig())
  )

  @Test
  fun `ignored properties are omitted in nested lists`() {
    val yaml = YamlConfiguration()

    ClassMapper.create(ContainerConfig::class, yaml)

    @Suppress("UNCHECKED_CAST")
    val saved = yaml.getList("offsets")!!.single() as Map<String, Any?>
    assertFalse(saved.containsKey("runtimeLabel"))
    assertFalse(saved.containsKey("vector"))
  }

  data class InvalidIgnoredConfig(
    @ConfigIgnore val runtimeValue: String
  )

  @Test
  fun `ignored constructor property requires a default`() {
    assertFailsWith<IllegalArgumentException> {
      ClassMapper.create(InvalidIgnoredConfig::class, YamlConfiguration())
    }
  }
}
