package com.peco2282.devcore.updatechecker

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import org.bukkit.plugin.Plugin

data class UpdateResult(
  val current: String,
  val latest: String,
  val url: String,
  val isNewer: Boolean
)

fun interface UpdateSource {
  fun request(client: HttpClient): CompletableFuture<Release>
}

data class Release(val version: String, val url: String)

class Github(private val repository: String) : UpdateSource {
  init { require(repository.matches(Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) { "repository must be owner/name" } }

  override fun request(client: HttpClient): CompletableFuture<Release> {
    val request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/$repository/releases/latest"))
      .timeout(Duration.ofSeconds(10))
      .header("Accept", "application/vnd.github+json")
      .header("User-Agent", "DevCore-UpdateChecker")
      .GET()
      .build()
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply { response ->
      check(response.statusCode() in 200..299) { "GitHub returned HTTP ${response.statusCode()}" }
      val tag = JSON_STRING.findAll(response.body()).associate { it.groupValues[1] to unescape(it.groupValues[2]) }["tag_name"]
        ?: error("GitHub response has no tag_name")
      Release(tag.removePrefix("v"), "https://github.com/$repository/releases/latest")
    }
  }

  private companion object {
    val JSON_STRING = Regex("\\\"([^\\\"]+)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
    fun unescape(value: String) = value.replace("\\\\/", "/").replace("\\\\\\\"", "\\\"").replace("\\\\\\\\", "\\\\")
  }
}

/**
 * Checks once, asynchronously. Network access is opt-in; with [enabled] false no request is made.
 * Failures are logged at FINE and complete with null, so startup is never blocked or failed.
 */
fun Plugin.checkForUpdates(
  source: UpdateSource,
  enabled: Boolean = false,
  client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
  callback: (UpdateResult) -> Unit = {}
): CompletableFuture<UpdateResult?> {
  if (!enabled) return CompletableFuture.completedFuture(null)
  val current = pluginMeta.version
  return source.request(client).thenApply { release ->
    UpdateResult(current, release.version, release.url, compareVersions(release.version, current) > 0).also(callback)
  }.exceptionally { failure ->
    logger.fine("Update check failed: ${failure.cause?.message ?: failure.message}")
    null
  }
}

internal fun compareVersions(left: String, right: String): Int {
  fun parts(value: String) = value.removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
  val a = parts(left)
  val b = parts(right)
  return (0 until maxOf(a.size, b.size)).firstNotNullOfOrNull { index ->
    (a.getOrElse(index) { 0 }).compareTo(b.getOrElse(index) { 0 }).takeIf { it != 0 }
  } ?: 0
}
