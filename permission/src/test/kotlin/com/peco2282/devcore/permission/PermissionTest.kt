package com.peco2282.devcore.permission
import be.seeseemelk.mockbukkit.MockBukkit
import org.bukkit.permissions.PermissionDefault
import kotlin.test.*

class PermissionTest {
  @BeforeTest fun setup() { MockBukkit.mock() }
  @AfterTest fun close() = MockBukkit.unmock()
  @Test fun `typed permissions register idempotently with children`() {
    val plugin = MockBukkit.createMockPlugin()
    val child = permission("test.use", PermissionDefault.TRUE)
    val admin = permission("test.admin", PermissionDefault.OP) { child(child) }
    plugin.registerPermissions(child, admin); plugin.registerPermissions(child, admin)
    assertEquals(true, plugin.server.pluginManager.getPermission("test.admin")!!.children["test.use"])
  }
  @Test fun `invalid node fails`() { assertFailsWith<IllegalArgumentException> { permission("Has Space") } }
}
