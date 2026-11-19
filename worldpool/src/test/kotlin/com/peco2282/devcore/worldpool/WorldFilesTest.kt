package com.peco2282.devcore.worldpool
import java.nio.file.Files
import kotlin.test.*

class WorldFilesTest {
  @Test fun `copy excludes runtime identity and lock files`() {
    val root = Files.createTempDirectory("worldpool")
    val source = Files.createDirectory(root.resolve("template")); val target = root.resolve("instance")
    Files.writeString(source.resolve("level.dat"), "data")
    Files.writeString(source.resolve("uid.dat"), "identity")
    Files.writeString(source.resolve("session.lock"), "lock")
    copyWorldTemplate(source, target)
    assertTrue(Files.exists(target.resolve("level.dat")))
    assertFalse(Files.exists(target.resolve("uid.dat")))
    assertFalse(Files.exists(target.resolve("session.lock")))
    deleteWorldDirectory(target, root)
    assertFalse(Files.exists(target))
  }
  @Test fun `delete refuses the pool root itself`() {
    val root = Files.createTempDirectory("worldpool-root")
    assertFailsWith<IllegalArgumentException> { deleteWorldDirectory(root, root) }
  }
}
