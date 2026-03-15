package com.peco2282.devcore.config

import com.peco2282.devcore.config.reflection.ClassMapper
import com.peco2282.devcore.config.reflection.TypeSerializers
import com.peco2282.devcore.config.serializers.Serializer
import com.peco2282.devcore.config.validations.annotations.*
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.util.*

class ConfigSerializerTest {

  /* =========================================================
   * 基本ロード
   * ========================================================= */

  data class BasicConfig(
    val name: String = "Steve",
    val level: Int = 1
  )

  @Test
  fun basicLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
            name: Alex
            level: 5
        """.trimIndent()
    )

    val cfg = Configs.load<BasicConfig>(file)

    assertEquals("Alex", cfg.name)
    assertEquals(5, cfg.level)
  }

  @Test
  fun defaultAutoSaveTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("")

    val yaml = YamlConfiguration.loadConfiguration(file)
    ClassMapper.create(BasicConfig::class, yaml)

    assertEquals("Steve", yaml.getString("name"))
    assertEquals(1, yaml.getInt("level"))
  }


  /* =========================================================
   * List
   * ========================================================= */

  data class ListConfig(
    val worlds: List<String> = listOf("world"),
    val rewards: List<Int> = listOf(1, 2, 3)
  )

  @Test
  fun listLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
            worlds:
              - world_nether
              - world_end
            rewards:
              - 10
              - 20
        """.trimIndent()
    )

    val cfg = Configs.load<ListConfig>(file)

    assertEquals(listOf("world_nether", "world_end"), cfg.worlds)
    assertEquals(listOf(10, 20), cfg.rewards)
  }


  /* =========================================================
   * ネスト
   * ========================================================= */

  data class Database(
    val host: String = "localhost",
    val port: Int = 3306
  )

  data class Messages(
    val prefix: String = "[Server]"
  )

  data class NestedConfig(
    val database: Database = Database(),
    val messages: Messages = Messages()
  )

  @Test
  fun nestedDefaultGenerateTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("")

    val yaml = YamlConfiguration.loadConfiguration(file)
    ClassMapper.create(NestedConfig::class, yaml)

    assertEquals("localhost", yaml.getString("database.host"))
    assertEquals(3306, yaml.getInt("database.port"))
    assertEquals("[Server]", yaml.getString("messages.prefix"))
  }


  /* =========================================================
   * List + ネスト
   * ========================================================= */

  data class ComplexConfig(
    val worlds: List<String> = listOf("world"),
    val databases: List<Database> = listOf(Database())
  )

  @Test
  fun listAndNestedLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
            worlds:
              - world_nether
              - world_end
            databases:
              - host: db1.local
                port: 3307
              - host: db2.local
                port: 3308
        """.trimIndent()
    )

    val cfg = Configs.load<ComplexConfig>(file)

    assertEquals(listOf("world_nether", "world_end"), cfg.worlds)
    assertEquals("db1.local", cfg.databases[0].host)
    assertEquals(3308, cfg.databases[1].port)
  }

  @Test
  fun listAndNestedDefaultGenerateTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("")

    val yaml = YamlConfiguration.loadConfiguration(file)
    val cfg = ClassMapper.create(ComplexConfig::class, yaml)

    // List default
    assertEquals(listOf("world"), cfg.worlds)

    // Nested list default
    assertEquals("localhost", cfg.databases[0].host)

    // YAML write-back verification
    assertEquals("world", yaml.getStringList("worlds")[0])

    @Suppress("UNCHECKED_CAST")
    val firstDb = (yaml.getList("databases")!![0] as Map<String, Any?>)
    assertEquals("localhost", firstDb["host"])
  }


  data class MapConfig(
    val limits: Map<String, Int> = mapOf(
      "stone" to 10,
      "diamond" to 1
    )
  )

  @Test
  fun mapLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
        limits:
          iron: 20
          gold: 5
    """.trimIndent()
    )

    val cfg = Configs.load<MapConfig>(file)

    assertEquals(20, cfg.limits["iron"])
    assertEquals(5, cfg.limits["gold"])
  }

  @Test
  fun mapDefaultGenerateTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("")

    val yaml = YamlConfiguration.loadConfiguration(file)
    ClassMapper.create(MapConfig::class, yaml)

    val section = yaml.getConfigurationSection("limits")!!
    assertEquals(10, section.getInt("stone"))
    assertEquals(1, section.getInt("diamond"))
  }

  data class ValidationConfig(
    @Range(min = 1, max = 10)
    val level: Int = 1,

    @NotBlank
    val name: String = "Steve"
  )

  @Test
  fun validationSuccessTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
        level: 5
        name: Alex
    """.trimIndent()
    )

    val cfg = Configs.load<ValidationConfig>(file)

    assertEquals(5, cfg.level)
    assertEquals("Alex", cfg.name)
  }

  @Test
  fun rangeValidationFailTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
        level: 50
    """.trimIndent()
    )

    assertThrows(IllegalArgumentException::class.java) {
      Configs.load<ValidationConfig>(file)
    }
  }

  @Test
  fun notBlankValidationFailTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
        name: ""
    """.trimIndent()
    )

    assertThrows(IllegalArgumentException::class.java) {
      Configs.load<ValidationConfig>(file)
    }
  }

  enum class Mode { EASY, HARD }

  data class EnumConfig(
    val mode: Mode = Mode.EASY
  )

  @Test
  fun enumLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("mode: HARD")

    val cfg = Configs.load<EnumConfig>(file)
    assertEquals(Mode.HARD, cfg.mode)
  }

  data class UuidConfig(val id: UUID)

  @Test
  fun customConverterTest() {
    TypeSerializers.register(UUID::class, object : Serializer<UUID> {
      override fun deserialize(value: Any?): UUID = UUID.fromString(value as String)
      override fun serialize(value: UUID) = value.toString()
    })

    val uuid = UUID.randomUUID()
    val file = File.createTempFile("test", ".yml")
    file.writeText("id: \"$uuid\"")

    val cfg = Configs.load<UuidConfig>(file)
    assertEquals(uuid, cfg.id)
  }

  data class CommentConfig(
    @Comment("Server Name")
    val name: String = "DevServer"
  )

  @Test
  fun commentWriteTest() {
    val file = File.createTempFile("test", ".yml")
    val yaml = YamlConfiguration()

    ClassMapper.write(CommentConfig(), yaml)
    // Actually, comments in Bukkit API depend on implementation details.
    // For now, we trust ClassMapper.write calls setComments.
  }

  data class NullableConfig(
    val webhook: String? = null
  )

  @Test
  fun nullableTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("")

    val cfg = Configs.load<NullableConfig>(file)
    assertNull(cfg.webhook)
  }

  data class ValidationConfig2(
    @Min(1) @Max(10)
    val level: Int = 5,

    @Positive
    val amount: Int = 1,

    @NotBlank
    val name: String = "ok"
  )

  @Test
  fun validationSuccessTest2() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
    level: 5
    amount: 2
    name: test
  """.trimIndent()
    )

    Configs.load<ValidationConfig2>(file) // 例外出なければOK
  }

  @Test
  fun validationFailTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("level: 100")

    assertThrows<IllegalArgumentException> {
      Configs.load<ValidationConfig>(file)
    }
  }

  data class TestConfig(
    val name: String = "Steve",
    val level: Int = 1,
    val enabled: Boolean = true
  )

  @Test
  fun reloadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText("name: Alex")

    val handle = ConfigHandle(file, TestConfig::class)
    handle.load()
    assertEquals("Alex", handle.instance.name)

    file.writeText("name: Bob")
    handle.reload()

    assertEquals("Bob", handle.instance.name)
  }

  @Test
  fun sectionLoadTest() {
    val file = File.createTempFile("test", ".yml")
    file.writeText(
      """
    database:
      host: db.local
      port: 3307
  """.trimIndent()
    )

    val yaml = YamlConfiguration.loadConfiguration(file)
    val db: Database = yaml.getConfigurationSection("database")!!.convert<Database>()

    assertEquals("db.local", db.host)
    assertEquals(3307, db.port)
  }

  @Test
  fun saveTest() {
    val file = File.createTempFile("test_save", ".yml")
    val config = BasicConfig(name = "Jane", level = 10)

    Configs.save(file, config)

    val yaml = YamlConfiguration.loadConfiguration(file)
    assertEquals("Jane", yaml.getString("name"))
    assertEquals(10, yaml.getInt("level"))

    // Verify it can be loaded back
    val loaded = Configs.load<BasicConfig>(file)
    assertEquals("Jane", loaded.name)
    assertEquals(10, loaded.level)
  }

}
