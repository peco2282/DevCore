package com.peco2282.devcore.worldpool

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Comparator

private val excluded = setOf("uid.dat", "session.lock")

internal fun copyWorldTemplate(source: Path, target: Path) {
  require(Files.isDirectory(source)) { "World template is not a directory: $source" }
  require(!Files.exists(target)) { "World instance already exists: $target" }
  Files.walk(source).use { paths ->
    paths.forEach { path ->
      val relative = source.relativize(path)
      if (relative.any { it.toString() in excluded }) return@forEach
      val destination = target.resolve(relative).normalize()
      require(destination.startsWith(target.normalize())) { "Template path escaped target root" }
      if (Files.isDirectory(path)) Files.createDirectories(destination)
      else Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES)
    }
  }
}

internal fun deleteWorldDirectory(target: Path, root: Path) {
  val normalized = target.toAbsolutePath().normalize()
  val normalizedRoot = root.toAbsolutePath().normalize()
  require(normalized != normalizedRoot && normalized.startsWith(normalizedRoot)) { "Refusing to delete outside world pool root" }
  if (!Files.exists(normalized)) return
  Files.walk(normalized).use { it.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
}
