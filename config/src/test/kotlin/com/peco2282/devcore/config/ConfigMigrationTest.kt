package com.peco2282.devcore.config

import com.peco2282.devcore.config.migration.ConfigVersion
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfigMigrationTest {
  data class MigratedConfig(
    @ConfigVersion val version: Int = 3,
    val message: String = "default",
    val ticks: Int = 0
  )

  @Test
  fun `migrations run in order and create an original-version backup`() {
    val file = Files.createTempFile("devcore-migration", ".yml").toFile()
    file.writeText("version: 1\nmsg: hello\ncooldown: 5\nlegacy: true\n")

    val config = Configs.load<MigratedConfig>(file) {
      migrations {
        from(1) { rename("msg", "message") }
        from(2) {
          rename("cooldown", "ticks")
          transform("ticks") { (it as Int) * 20 }
          remove("legacy")
        }
      }
    }

    assertEquals(MigratedConfig(message = "hello", ticks = 100), config)
    assertTrue(file.resolveSibling("${file.name}.bak-v1").exists())
    assertTrue("version: 3" in file.readText())
  }

  @Test
  fun `failed migration restores the original file`() {
    val file = Files.createTempFile("devcore-migration-fail", ".yml").toFile()
    val original = "version: 1\nmsg: hello\n"
    file.writeText(original)

    assertFailsWith<IllegalStateException> {
      Configs.load<MigratedConfig>(file) {
        migrations { from(1) { error("broken") } }
      }
    }

    assertEquals(original, file.readText())
  }
}
