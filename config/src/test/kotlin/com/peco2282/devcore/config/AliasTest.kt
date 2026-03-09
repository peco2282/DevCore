package com.peco2282.devcore.config

import com.peco2282.devcore.config.validations.annotations.Alias
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class AliasTest {

  data class AliasConfig(
    @Alias("old_name")
    val newName: String = "default",
    @Alias("old_level")
    val level: Int = 1
  )

  @Test
  fun testAliasLoad() {
    val file = File.createTempFile("test_alias", ".yml")
    file.writeText(
      """
            old_name: "aliased_value"
            old_level: 10
        """.trimIndent()
    )

    val cfg = Configs.load<AliasConfig>(file)

    assertEquals("aliased_value", cfg.newName)
    assertEquals(10, cfg.level)

    // Verify migration (auto-save with new name)
    // Note: Configs.load uses internal auto-save if file is provided.
    val yaml = YamlConfiguration.loadConfiguration(file)
    println("[DEBUG_LOG] YAML content: ${yaml.saveToString()}")
    assertEquals("aliased_value", yaml.getString("newName"))
    assertEquals(10, yaml.getInt("level"))
    
    // old keys should still exist in yaml object until we save it and reload it if we use Bukkit's ConfigurationSection.
    // However, ClassMapper.write will set the new name.
  }

  data class NestedAlias(
    val info: AliasConfig = AliasConfig()
  )

  @Test
  fun testNestedAliasLoad() {
    val file = File.createTempFile("test_nested_alias", ".yml")
    file.writeText(
      """
            info:
              old_name: "nested_aliased"
        """.trimIndent()
    )

    val cfg = Configs.load<NestedAlias>(file)

    assertEquals("nested_aliased", cfg.info.newName)

    val yaml = YamlConfiguration.loadConfiguration(file)
    assertEquals("nested_aliased", yaml.getString("info.newName"))
  }
}
