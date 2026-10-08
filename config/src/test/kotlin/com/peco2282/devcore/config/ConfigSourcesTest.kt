package com.peco2282.devcore.config

import com.peco2282.devcore.config.validations.annotations.ConfigKey
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ConfigSourcesTest {

  data class ServiceConfig(
    @ConfigKey("max-retries")
    val maxRetries: Int = 3,

    @ConfigKey("request-timeout")
    val requestTimeout: Int = 30,

    @ConfigKey("base-url")
    val baseUrl: String? = null,

    val enabled: Boolean? = null
  )

  @Test
  fun `earlier section wins and missing values fall through`() {
    val overrides = YamlConfiguration().apply {
      set("request-timeout", 45)
    }
    val defaults = YamlConfiguration().apply {
      set("request-timeout", 60)
      set("base-url", "https://example.com")
      set("enabled", true)
    }

    val config = Configs.from(overrides, defaults).convert<ServiceConfig>()

    assertEquals(45, config.requestTimeout)
    assertEquals("https://example.com", config.baseUrl)
    assertEquals(true, config.enabled)
    assertEquals(3, config.maxRetries)
  }

  @Test
  fun `explicit zero wins over lower priority value`() {
    val overrides = YamlConfiguration().apply {
      set("request-timeout", 0)
    }
    val defaults = YamlConfiguration().apply {
      set("request-timeout", 60)
    }

    val config = Configs.from(overrides, defaults).convert<ServiceConfig>()

    assertEquals(0, config.requestTimeout)
  }

  @Test
  fun `constructor defaults are used after all sources are exhausted`() {
    val config = Configs.from(null, YamlConfiguration()).convert<ServiceConfig>()

    assertEquals(3, config.maxRetries)
    assertEquals(30, config.requestTimeout)
    assertNull(config.baseUrl)
    assertNull(config.enabled)
  }

  data class RootConfig(
    val service: ServiceConfig = ServiceConfig()
  )

  @Test
  fun `nested sections are merged recursively`() {
    val overrides = YamlConfiguration().apply {
      set("service.request-timeout", 45)
    }
    val defaults = YamlConfiguration().apply {
      set("service.request-timeout", 60)
      set("service.enabled", true)
    }

    val config = Configs.from(overrides, defaults).convert<RootConfig>()

    assertEquals(45, config.service.requestTimeout)
    assertEquals(true, config.service.enabled)
    assertEquals(3, config.service.maxRetries)
  }
}
