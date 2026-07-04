package com.peco2282.devcore.i18n

import org.bukkit.plugin.Plugin
import java.io.File
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Tests for the I18N internationalization system.
 */
class I18NTest {
  /** Temporary directory for test files. */
  private lateinit var tempDir: File

  /** I18N instance under test. */
  private lateinit var i18n: I18N

  /**
   * Creates a temporary directory before each test.
   */
  @BeforeTest
  fun setup() {
    tempDir = Files.createTempDirectory("i18n-test").toFile()
  }

  /**
   * Deletes the temporary directory after each test.
   */
  @AfterTest
  fun tearDown() {
    tempDir.deleteRecursively()
  }

  /**
   * Tests loading locale files and translating messages.
   */
  @Test
  fun testLoadLocalesAndTranslate() {
    val i18n = I18N(dummyPlugin(), tempDir)

    val enFile = File(tempDir, "en_US.yml")
    enFile.writeText(
      """
            test:
              message: "Hello {0}!"
              simple: "Welcome"
        """.trimIndent()
    )

    val jaFile = File(tempDir, "ja_JP.yml")
    jaFile.writeText(
      """
            test:
              message: "こんにちは {0}さん！"
              simple: "ようこそ"
        """.trimIndent()
    )

    i18n.loadLocales()

    assertEquals("Welcome", i18n.translate(Locale.US, "test.simple"))
    assertEquals("Hello Junie!", i18n.translate(Locale.US, "test.message", "Junie"))

    assertEquals("ようこそ", i18n.translate(Locale.JAPAN, "test.simple"))
    assertEquals("こんにちは Junieさん！", i18n.translate(Locale.JAPAN, "test.message", "Junie"))
  }

  /**
   * Creates a dummy plugin for testing purposes.
   */
  private fun dummyPlugin(): Plugin {
    return Proxy.newProxyInstance(
      Plugin::class.java.classLoader,
      arrayOf(Plugin::class.java)
    ) { _, method, _ ->
      when (method.name) {
        "getName" -> "TestPlugin"
        "getDataFolder" -> tempDir
        else -> null
      }
    } as Plugin
  }
}
