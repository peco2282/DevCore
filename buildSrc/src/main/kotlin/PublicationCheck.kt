import org.gradle.api.GradleException
import java.net.HttpURLConnection
import java.net.URI
import java.util.Base64

object PublicationCheck {
  fun exists(repository: URI, artifactPath: String, username: String?, password: String?): Boolean {
    val target = URI(repository.toString().trimEnd('/') + "/").resolve(artifactPath)
    if (target.scheme == "file") return java.io.File(target).exists()
    val connection = target.toURL().openConnection() as? HttpURLConnection
      ?: throw GradleException("Unsupported publication repository protocol: ${target.scheme}")
    try {
      connection.requestMethod = "HEAD"
      connection.connectTimeout = 5000
      connection.readTimeout = 5000
      connection.instanceFollowRedirects = false
      if (username != null && password != null) {
        val token = Base64.getEncoder().encodeToString("$username:$password".toByteArray(Charsets.UTF_8))
        connection.setRequestProperty("Authorization", "Basic $token")
      }
      return when (val status = connection.responseCode) {
        200 -> true
        404 -> false
        else -> throw GradleException("Publication check failed (HTTP $status); publishing aborted")
      }
    } catch (e: java.io.IOException) {
      throw GradleException("Publication check failed; publishing aborted", e)
    } finally {
      connection.disconnect()
    }
  }
}