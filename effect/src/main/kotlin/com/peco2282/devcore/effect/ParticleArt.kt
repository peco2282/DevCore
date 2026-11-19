package com.peco2282.devcore.effect

import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import org.bukkit.util.Vector

/**
 * Rasterizes [text] into a reusable particle [Shape].
 *
 * Rendering uses an in-memory ARGB image and therefore works with `java.awt.headless=true`.
 * Every [sampleStep] pixels, an alpha value at or above [alphaThreshold] becomes one point in the
 * XY plane. Coordinates are multiplied by [scale], the image Y axis is flipped so text is upright,
 * and generation stops at [maxPoints]. Increase [sampleStep] or reduce the font to lower cost.
 *
 * ```kotlin
 * val logo = textShape(
 *   "DevCore",
 *   scale = 0.06,
 *   font = Font(Font.SANS_SERIF, Font.BOLD, 32),
 *   sampleStep = 3,
 *   maxPoints = 300
 * )
 * effect { particle(Particle.DUST, logo, data = dust(Color.AQUA)) }
 * ```
 *
 * The returned shape stores geometry, not color. Use particle data to choose color at playback.
 */
fun textShape(
  text: String,
  scale: Double = 0.1,
  font: Font = Font(Font.SANS_SERIF, Font.BOLD, 32),
  sampleStep: Int = 2,
  alphaThreshold: Int = 32,
  maxPoints: Int = 5_000
): Shape {
  require(text.isNotEmpty()) { "text must not be empty" }
  val metricsImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
  val metrics = metricsImage.createGraphics().run { try { this.font = font; fontMetrics } finally { dispose() } }
  val image = BufferedImage(maxOf(1, metrics.stringWidth(text) + 4), maxOf(1, metrics.height + 4), BufferedImage.TYPE_INT_ARGB)
  image.createGraphics().use { graphics ->
    graphics.font = font
    graphics.color = Color.WHITE
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
    graphics.drawString(text, 2, 2 + graphics.fontMetrics.ascent)
  }
  return imageShape(image, image.width, scale, sampleStep, alphaThreshold, maxPoints)
}

/**
 * Reads [file] with [ImageIO] and converts visible pixels into points.
 *
 * [width] is the sampled output width, while height is derived from the source aspect ratio.
 * [pixelSize] controls the distance between adjacent output pixels in blocks. See the
 * [BufferedImage overload][imageShape] for sampling and alpha semantics.
 *
 * @throws IllegalArgumentException when the file format is unsupported or an option is invalid.
 */
fun imageShape(
  file: File,
  width: Int = 20,
  pixelSize: Double = 0.1,
  sampleStep: Int = 1,
  alphaThreshold: Int = 32,
  maxPoints: Int = 5_000
): Shape = imageShape(requireNotNull(ImageIO.read(file)) { "Unsupported image: $file" }, width, pixelSize, sampleStep, alphaThreshold, maxPoints)

/**
 * Converts visible pixels of [image] into points in the XY plane.
 *
 * The image is sampled at an aspect-ratio-preserving size. [sampleStep] skips output pixels in
 * both axes, [alphaThreshold] ignores transparent pixels, and [maxPoints] is a hard safety limit.
 * Calls to `points()` return cloned vectors, so callers may transform them safely.
 *
 * ```kotlin
 * val icon = imageShape(image, width = 32, pixelSize = 0.05, sampleStep = 2, maxPoints = 500)
 * val centered = icon.offset(x = -0.8, y = 1.0)
 * ```
 */
fun imageShape(
  image: BufferedImage,
  width: Int = image.width,
  pixelSize: Double = 0.1,
  sampleStep: Int = 1,
  alphaThreshold: Int = 32,
  maxPoints: Int = 5_000
): Shape {
  require(width > 0 && pixelSize > 0 && sampleStep > 0 && maxPoints > 0) { "dimensions, sampling and limit must be positive" }
  require(alphaThreshold in 0..255) { "alphaThreshold must be between 0 and 255" }
  val height = maxOf(1, (image.height.toDouble() * width / image.width).toInt())
  val sampled = ArrayList<Vector>(minOf(maxPoints, width * height))
  for (y in 0 until height step sampleStep) for (x in 0 until width step sampleStep) {
    val sourceX = x * image.width / width
    val sourceY = y * image.height / height
    if ((image.getRGB(sourceX, sourceY) ushr 24) >= alphaThreshold) {
      sampled += Vector(x * pixelSize, (height - 1 - y) * pixelSize, 0.0)
      if (sampled.size == maxPoints) return Shape { sampled.map(Vector::clone) }
    }
  }
  return Shape { sampled.map(Vector::clone) }
}

private inline fun <T : java.awt.Graphics> T.use(block: (T) -> Unit) { try { block(this) } finally { dispose() } }
