package com.peco2282.devcore.updatechecker

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import org.bukkit.plugin.Plugin

/** Result of comparing the plugin's installed version with a remote release. */
data class UpdateResult(
  val current: String,
  val latest: String,
  val url: String,
  val isNewer: Boolean
)

/**
 * A provider of the newest available release.
 *
 * Implementations must return immediately and perform I/O through the supplied asynchronous
 * [HttpClient]. Complete the future exceptionally for transport, status or parsing failures;
 * [checkForUpdates] converts those failures to a harmless `null` result.
 */
fun interface UpdateSource {
  fun request(client: HttpClient): CompletableFuture<Release>
}

/** A version string and the human-facing page where that release can be obtained. */
data class Release(val version: String, val url: String)

/**
 * Reads the latest published release from GitHub's REST API.
 *
 * [repository] must be an `owner/name` pair. Tags may start with `v`; that prefix is removed
 * before comparison. Draft-only repositories or projects without a published release result in
 * a failed request, which [checkForUpdates] deliberately treats as non-fatal.
 *
 * ```kotlin
 * plugin.checkForUpdates(Github("peco2282/DevCore"), enabled = true) { result ->
 *   if (result.isNewer) logger.info("Update: ${result.url}")
 * }
 * ```
 */
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
 * Checks [source] once without blocking the server thread.
 *
 * Network access is explicitly opt-in: when [enabled] is false this function does not construct
 * or send an HTTP request and returns an already completed future containing `null`. When enabled,
 * [callback] runs on the `HttpClient` completion thread, not the Bukkit main thread. Schedule back
 * onto Bukkit before reading or mutating world, entity, inventory, or other server state.
 *
 * Version comparison is numeric per dot-separated component (`1.21.10` is newer than `1.21.4`).
 * A leading `v` and a pre-release suffix are ignored. Network, HTTP and parsing errors are logged
 * at `FINE`, complete as `null`, and never disable the plugin.
 *
 * ```kotlin
 * plugin.checkForUpdates(
 *   Github("owner/plugin"),
 *   enabled = config.getBoolean("update-checker.enabled", false)
 * ) { result ->
 *   if (result.isNewer) logger.info("${result.latest}: ${result.url}")
 * }
 * ```
 *
 * @return a future containing the comparison, or `null` when disabled or unsuccessful.
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
