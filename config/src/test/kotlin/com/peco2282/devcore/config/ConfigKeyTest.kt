package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import com.peco2282.devcore.config.validations.annotations.Alias
import com.peco2282.devcore.config.validations.annotations.ConfigKey
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class ConfigKeyTest {

  data class ServerConfig(
    @ConfigKey("request-timeout")
    val requestTimeout: Int = 30
  )

  @Test
  fun `canonical key is used for reading and writing`() {
    val yaml = YamlConfiguration().apply {
      set("request-timeout", 45)
    }

    val config = ClassMapper.create(ServerConfig::class, yaml)

    assertEquals(45, config.requestTimeout)
    assertEquals(45, yaml.getInt("request-timeout"))
    assertFalse(yaml.contains("requestTimeout"))
  }

  data class RenamedConfig(
    @ConfigKey("request-timeout")
    @Alias("timeout")
    val requestTimeout: Int = 30
  )

  @Test
  fun `alias is read but canonical key is written`() {
    val yaml = YamlConfiguration().apply {
      set("timeout", 20)
    }

    val config = ClassMapper.create(RenamedConfig::class, yaml)

    assertEquals(20, config.requestTimeout)
    assertEquals(20, yaml.getInt("request-timeout"))
    assertFalse("requestTimeout" in yaml)
  }

  data class Entry(
    @ConfigKey("max-attempts")
    val maxAttempts: Int = 1
  )

  data class ListConfig(
    val entries: List<Entry> = emptyList()
  )

  @Test
  fun `canonical key is honored inside data class lists`() {
    val yaml = YamlConfiguration().apply {
      set("entries", listOf(mapOf("max-attempts" to 5)))
    }

    val config = ClassMapper.create(ListConfig::class, yaml)

    assertEquals(5, config.entries.single().maxAttempts)
    @Suppress("UNCHECKED_CAST")
    val saved = yaml.getList("entries")!!.single() as Map<String, Any?>
    assertEquals(5, saved["max-attempts"])
    assertFalse(saved.containsKey("maxAttempts"))
  }
}
