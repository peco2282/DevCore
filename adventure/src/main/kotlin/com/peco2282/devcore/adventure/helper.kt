package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.util.Ticks
import java.time.Duration

fun Audience.send(component: Component) = sendMessage(component)
fun Collection<Audience>.send(component: Component) = forEach { it.sendMessage(component) }

fun Audience.send(consumer: ComponentBuilder.() -> Unit) = sendMessage(component(consumer))
fun Collection<Audience>.send(consumer: ComponentBuilder.() -> Unit) = forEach { it.sendMessage(component(consumer)) }

fun Audience.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) =
  sendMessage(component(joiner, consumer))

fun Collection<Audience>.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) =
  forEach { it.sendMessage(component(joiner, consumer)) }


fun Audience.actionBar(consumer: ComponentBuilder.() -> Unit) = sendActionBar(component(consumer))

fun Collection<Audience>.actionBar(consumer: ComponentBuilder.() -> Unit) = forEach { it.actionBar(consumer) }

internal fun toDuration(seconds: Double): Duration {
  require(seconds.isFinite() && seconds >= 0.0) { "Seconds must be finite and non-negative" }
  return Duration.ofMillis((seconds * 1000).toLong())
}

internal fun fromDuration(duration: Duration) = duration.toMillis() / 1000.0

fun Audience.title(
  fadeInSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeIn()),
  staySeconds: Double = fromDuration(Title.DEFAULT_TIMES.stay()),
  fadeOutSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeOut()),
  title: ComponentBuilder.() -> Unit,
  subtitle: ComponentBuilder.() -> Unit
) {
  showTitle(
    Title.title(
      component(title),
      component(subtitle),
      Title.Times.times(
        toDuration(fadeInSeconds),
        toDuration(staySeconds),
        toDuration(fadeOutSeconds)
      )
    )
  )
}

fun Collection<Audience>.title(
  fadeInSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeIn()),
  staySeconds: Double = fromDuration(Title.DEFAULT_TIMES.stay()),
  fadeOutSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeOut()),
  title: ComponentBuilder.() -> Unit,
  subtitle: ComponentBuilder.() -> Unit
) {
  forEach {
    it.title(
      fadeInSeconds,
      staySeconds,
      fadeOutSeconds,
      title,
      subtitle
    )
  }
}

fun Audience.title(
  builder: TitleBuilder.() -> Unit
) = showTitle(createTitle(builder))

fun Collection<Audience>.title(
  builder: TitleBuilder.() -> Unit
) {
  forEach { it.title(builder) }
}

fun createTitle(builder: TitleBuilder.() -> Unit): Title = TitleBuilder.Companion.Builder().apply(builder).build()

fun Audience.updateTitle(
  title: Component? = null,
  subtitle: Component? = null,
  times: Title.Times? = null
) {
  title?.let { sendTitlePart(TitlePart.TITLE, it) }
  subtitle?.let { sendTitlePart(TitlePart.SUBTITLE, it) }
  times?.let { sendTitlePart(TitlePart.TIMES, it) }
}

fun Collection<Audience>.updateTitle(
  title: Component? = null,
  subtitle: Component? = null,
  times: Title.Times? = null
) = forEach { it.updateTitle(title, subtitle, times) }

fun Collection<Audience>.clearTitle() = forEach { it.clearTitle() }

fun Collection<Audience>.resetTitle() = forEach { it.resetTitle() }

interface TitleBuilder {
  var fadeIn: Long
  var fadeInSeconds: Double

  var stay: Long
  var staySeconds: Double

  var fadeOut: Long
  var fadeOutSeconds: Double

  fun title(consumer: ComponentBuilder.() -> Unit)
  fun subtitle(consumer: ComponentBuilder.() -> Unit)

  companion object {
    internal class Builder : TitleBuilder {
      val def: Long = -1L
      private fun tickOrSec(tick: Long, sec: Double, default: Duration): Duration = when {
        tick != def -> {
          require(tick >= 0) { "Ticks must be non-negative" }
          Ticks.duration(tick)
        }

        sec != def.toDouble() -> toDuration(sec)
        else -> default
      }

      private var title: Component = Component.empty()
      private var subtitle: Component = Component.empty()

      override var fadeIn = def
      override var stay = def
      override var fadeOut = def

      override var fadeInSeconds: Double = def.toDouble()
      override var staySeconds: Double = def.toDouble()
      override var fadeOutSeconds: Double = def.toDouble()

      override fun title(consumer: ComponentBuilder.() -> Unit) {
        title = component(consumer)
      }

      override fun subtitle(consumer: ComponentBuilder.() -> Unit) {
        subtitle = component(consumer)
      }

      fun build() = Title.title(
        title,
        subtitle,
        Title.Times.times(
          tickOrSec(fadeIn, fadeInSeconds, Title.DEFAULT_TIMES.fadeIn()),
          tickOrSec(stay, staySeconds, Title.DEFAULT_TIMES.stay()),
          tickOrSec(fadeOut, fadeOutSeconds, Title.DEFAULT_TIMES.fadeOut())
        )
      )
    }
  }
}
