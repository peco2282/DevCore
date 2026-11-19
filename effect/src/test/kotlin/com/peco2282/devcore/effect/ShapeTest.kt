package com.peco2282.devcore.effect

import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ShapeTest {
  private val eps = 1e-9

  private fun assertVec(x: Double, y: Double, z: Double, v: Vector) {
    assertEquals(x, v.x, 1e-6); assertEquals(y, v.y, 1e-6); assertEquals(z, v.z, 1e-6)
  }

  @Test
  fun `line includes both endpoints and respects density`() {
    val pts = line(Vector(4.0, 0.0, 0.0), density = 1.0).points()
    assertEquals(5, pts.size)
    assertVec(0.0, 0.0, 0.0, pts.first())
    assertVec(4.0, 0.0, 0.0, pts.last())
  }

  @Test
  fun `zero length line still yields two points`() {
    assertEquals(2, line(Vector(0, 0, 0)).points().size)
  }

  @Test
  fun `flat circle stays on the xz plane at the given radius`() {
    val pts = circle(2.0, points = 12).points()
    assertEquals(12, pts.size)
    pts.forEach {
      assertEquals(0.0, it.y, eps)
      assertEquals(2.0, it.length(), 1e-6)
    }
  }

  @Test
  fun `circle points are distinct, full circle does not repeat the first point`() {
    val pts = circle(1.0, points = 8).points()
    assertEquals(8, pts.map { "%.4f,%.4f,%.4f".format(it.x, it.y, it.z) }.toSet().size)
  }

  @Test
  fun `circle honours an arbitrary normal`() {
    val normal = Vector(1.0, 1.0, 0.0)
    circle(3.0, normal = normal).points().forEach {
      assertEquals(0.0, it.dot(normal), 1e-6)
      assertEquals(3.0, it.length(), 1e-6)
    }
  }

  @Test
  fun `circle works when the normal is vertical or horizontal`() {
    listOf(Vector(0, 1, 0), Vector(0, -1, 0), Vector(1, 0, 0)).forEach { n ->
      circle(1.0, normal = n).points().forEach { assertEquals(1.0, it.length(), 1e-6) }
    }
  }

  @Test
  fun `partial arc generates proportionally fewer points including the end`() {
    val pts = circle(1.0, points = 40, arc = PI).points()
    assertEquals(21, pts.size)
    // A half circle's endpoints are a diameter apart.
    assertEquals(2.0, pts.first().distance(pts.last()), 1e-6)
  }

  @Test
  fun `sphere points all lie on the surface`() {
    val pts = sphere(5.0, points = 200).points()
    assertEquals(200, pts.size)
    pts.forEach { assertEquals(5.0, it.length(), 1e-6) }
  }

  @Test
  fun `helix spans the requested height`() {
    val pts = helix(1.0, height = 3.0, turns = 2.0, points = 31).points()
    assertEquals(0.0, pts.first().y, eps)
    assertEquals(3.0, pts.last().y, 1e-9)
    pts.forEach { assertEquals(1.0, Vector(it.x, 0.0, it.z).length(), 1e-6) }
  }

  @Test
  fun `transforms do not mutate the source and compose`() {
    val base = line(Vector(1.0, 0.0, 0.0), density = 1.0)
    val moved = base.rotateY(PI / 2).scale(2.0).offset(y = 1.0).points()
    // (1,0,0) rotated 90deg about Y -> (0,0,-1); scaled -> (0,0,-2); offset -> (0,1,-2)
    assertVec(0.0, 1.0, -2.0, moved.last())
    assertVec(1.0, 0.0, 0.0, base.points().last())
  }

  @Test
  fun `plus concatenates shapes`() {
    assertEquals(3, (point() + line(Vector(1.0, 0.0, 0.0), density = 1.0)).points().size)
  }

  @Test
  fun `repeated points calls return independent vectors`() {
    val shape = circle(1.0)
    shape.points().forEach { it.multiply(100.0) }
    assertTrue(shape.points().all { it.length() < 1.01 })
  }

  @Test
  fun `invalid arguments are rejected`() {
    assertFailsWith<IllegalArgumentException> { line(Vector(1, 0, 0), density = 0.0) }
    assertFailsWith<IllegalArgumentException> { circle(1.0, points = 0) }
    assertFailsWith<IllegalArgumentException> { circle(1.0, normal = Vector(0, 0, 0)) }
    assertFailsWith<IllegalArgumentException> { helix(1.0, 1.0, points = 1) }
  }
}
