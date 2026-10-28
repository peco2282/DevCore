import com.sun.net.httpserver.HttpServer
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URI
import java.util.Base64

class PublicationCheckTest {
  @Test
  fun `checks normalized paths with authentication and fails closed`() {
    val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
    var status = 200
    var path = ""
    var authorization: String? = null
    server.createContext("/") { exchange ->
      path = exchange.requestURI.path
      authorization = exchange.requestHeaders.getFirst("Authorization")
      assertEquals("HEAD", exchange.requestMethod)
      exchange.sendResponseHeaders(status, -1)
      exchange.close()
    }
    server.start()
    val base = "http://127.0.0.1:${server.address.port}/releases"
    try {
      for (suffix in listOf("", "/")) {
        assertTrue(PublicationCheck.exists(URI(base + suffix), "group/module/1/module-1.pom", "user", "secret"))
        assertEquals("/releases/group/module/1/module-1.pom", path)
        assertEquals("Basic " + Base64.getEncoder().encodeToString("user:secret".toByteArray()), authorization)
      }
      status = 404
      assertFalse(PublicationCheck.exists(URI(base), "missing.pom", null, null))
      for (code in listOf(301, 401, 403, 405, 500)) {
        status = code
        assertThrows(GradleException::class.java) {
          PublicationCheck.exists(URI(base), "module.pom", null, null)
        }
      }
    } finally {
      server.stop(0)
    }
    assertThrows(GradleException::class.java) {
      PublicationCheck.exists(URI(base), "module.pom", null, null)
    }
  }
}