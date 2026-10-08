package com.peco2282.devcore.config

import com.peco2282.devcore.config.validations.annotations.ConfigKey
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigReaderTest {

  data class ServiceConfig(
    val enabled: Boolean = true,
    @ConfigKey("max-attempts") val maxAttempts: Int = 3
  )

  data class LimitsConfig(
    @ConfigKey("max-attempts") val maxAttempts: Int,
    @ConfigKey("timeout-seconds") val timeoutSeconds: Double
  )

  data class ApplicationConfig(
    val limits: LimitsConfig
  )

  @Test
  fun `file reader writes defaults by default`() {
    val file = File.createTempFile("config-reader", ".yml").apply {
      writeText("enabled: false")
    }

    val config = ConfigReader(file).read<ServiceConfig>()
    val saved = YamlConfiguration.loadConfiguration(file)

    assertFalse(config.enabled)
    assertEquals(3, config.maxAttempts)
    assertEquals(3, saved.getInt("max-attempts"))
  }

  @Test
  fun `file reader can disable write back`() {
    val file = File.createTempFile("config-reader-read-only", ".yml").apply {
      writeText("enabled: false\n")
    }

    val config = ConfigReader(file)
      .writeDefaults(false)
      .read<ServiceConfig>()
    val saved = YamlConfiguration.loadConfiguration(file)

    assertFalse(config.enabled)
    assertEquals(3, config.maxAttempts)
    assertFalse(saved.contains("max-attempts"))
  }

  @Test
  fun `file reader can target a nested section`() {
    val file = File.createTempFile("config-reader-section", ".yml").apply {
      writeText("services:\n  primary:\n    enabled: false\n")
    }

    val config = ConfigReader(file)
      .section("services.primary")
      .read<ServiceConfig>()
    val saved = YamlConfiguration.loadConfiguration(file)

    assertFalse(config.enabled)
    assertEquals(3, saved.getInt("services.primary.max-attempts"))
  }

  @Test
  fun `section reader preserves source priority without mutating sources`() {
    val overrides = YamlConfiguration().apply {
      set("services.primary.enabled", false)
    }
    val defaults = YamlConfiguration().apply {
      set("services.primary.enabled", true)
      set("services.primary.max-attempts", 5)
    }

    val config = ConfigReader(overrides, defaults)
      .section("services.primary")
      .read<ServiceConfig>()

    assertFalse(config.enabled)
    assertEquals(5, config.maxAttempts)
    assertFalse(overrides.contains("services.primary.max-attempts"))
    assertTrue(defaults.contains("services.primary.max-attempts"))
  }

  @Test
  fun `file reader can place a root section below the selected section as fallback`() {
    val file = File.createTempFile("config-reader-nested-fallback", ".yml").apply {
      writeText(
        """
          defaults:
            limits:
              max-attempts: 5
              timeout-seconds: 30.0
          services:
            primary:
              limits:
                timeout-seconds: 10.0
        """.trimIndent()
      )
    }

    val config = ConfigReader(file)
      .section("services.primary")
      .fallbackSection("limits", "defaults.limits")
      .writeDefaults(false)
      .read<ApplicationConfig>()
    val saved = YamlConfiguration.loadConfiguration(file)

    assertEquals(5, config.limits.maxAttempts)
    assertEquals(10.0, config.limits.timeoutSeconds)
    assertFalse(saved.contains("services.primary.limits.max-attempts"))
  }

  @Test
  fun `reader can place an explicit section as nested fallback`() {
    val service = YamlConfiguration().apply {
      set("limits.timeout-seconds", 10.0)
    }
    val sharedLimits = YamlConfiguration().apply {
      set("max-attempts", 5)
      set("timeout-seconds", 30.0)
    }

    val config = ConfigReader(service)
      .fallbackSection("limits", sharedLimits)
      .read<ApplicationConfig>()

    assertEquals(5, config.limits.maxAttempts)
    assertEquals(10.0, config.limits.timeoutSeconds)
    assertFalse(service.contains("limits.max-attempts"))
  }
}
