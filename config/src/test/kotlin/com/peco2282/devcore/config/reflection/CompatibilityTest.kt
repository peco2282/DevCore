package com.peco2282.devcore.config.reflection

import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class CompatibilityTest {

    data class ConfigV1(
        val name: String = "Steve",
        val level: Int = 1
    )

    data class ConfigV2(
        val name: String = "Steve",
        val level: Int = 1,
        val newField: String = "DefaultValue"
    )

    @Test
    fun testForwardCompatibility() {
        // V1の構成で保存
        val yaml = YamlConfiguration()
        val v1 = ConfigV1(name = "Alex", level = 10)
        ClassMapper.write(v1, yaml)
        
        println("[DEBUG_LOG] YAML content before create: ${yaml.saveToString()}")
        
        // V2として読み込み
        // 現状のコードでは newField が YAML にないため、デフォルト値が使われるべき
        val v2 = ClassMapper.create(ConfigV2::class, yaml)
        
        println("[DEBUG_LOG] Loaded V2: $v2")
        println("[DEBUG_LOG] YAML content after create: ${yaml.saveToString()}")
        
        assertEquals("Alex", v2.name)
        assertEquals(10, v2.level)
        assertEquals("DefaultValue", v2.newField, "New field should use default value when missing in YAML")
        
        // 読み込み後に自動保存（create内部で呼ばれる）されているはずなので、YAMLに反映されているか確認
        assertTrue(yaml.contains("newField"), "YAML should now contain the new field with default value")
        assertEquals("DefaultValue", yaml.getString("newField"))
    }
}
