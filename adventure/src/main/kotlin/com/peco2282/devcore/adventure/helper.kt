package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import net.kyori.adventure.util.Ticks
import java.time.Duration

/**
 * Sends an already constructed Adventure component as a message to this audience.
 *
 * The component is passed directly to [Audience.sendMessage], preserving its children, styling,
 * and events. This overload does not create a component builder or modify the supplied component.
 * Use the lambda overload when the message should be assembled with the component DSL instead.
 *
 * ```kotlin
 * player.send(Component.text("Welcome!"))
 * ```
 *
 * @param component the complete message to deliver.
 */
fun Audience.send(component: Component) = sendMessage(component)

/**
 * Sends the same prebuilt component to every audience in this collection.
 *
 * Delivery follows collection iteration order and reuses [component] rather than rebuilding it
 * for each recipient. An empty collection performs no sends. Entries are not deduplicated, so
 * an audience appearing more than once receives a message for each occurrence.
 *
 * ```kotlin
 * audiences.send(Component.text("The game is starting."))
 * ```
 *
 * @param component the complete message shared by all recipients.
 */
fun Collection<Audience>.send(component: Component) = forEach { it.sendMessage(component) }

/**
 * Builds a message with the component DSL and sends it to this audience.
 *
 * [consumer] runs immediately with a fresh [ComponentBuilder] as its receiver. Append text or
 * components inside the block; the collected parts are joined with the default join behavior
 * and the resulting component is passed to [Audience.sendMessage]. The block is not retained.
 *
 * ```kotlin
 * player.send {
 *   +"Welcome, "
 *   +Component.text(player.name)
 * }
 * ```
 *
 * @param consumer the component-building block, executed once for this message.
 */
fun Audience.send(consumer: ComponentBuilder.() -> Unit) = sendMessage(component(consumer))

/**
 * Builds and sends a DSL message independently for each audience in this collection.
 *
 * A fresh [ComponentBuilder] is created and [consumer] is executed for each entry, in iteration
 * order. Captured state and side effects are therefore evaluated once per recipient, not once
 * for the whole collection. The receiver of the block is the builder, not the current audience.
 * An empty collection does not execute the block; duplicate entries are processed repeatedly.
 *
 * ```kotlin
 * audiences.send {
 *   +"Server announcement: "
 *   +"Have fun!"
 * }
 * ```
 *
 * @param consumer the component-building block to run separately for every entry.
 */
fun Collection<Audience>.send(consumer: ComponentBuilder.() -> Unit) {
  val component = component(consumer)
  forEach { it.sendMessage(component) }
}

/**
 * Builds a DSL message and controls how its accumulated parts become the final component.
 *
 * First [consumer] populates a fresh [ComponentBuilder]; then [joiner] receives that builder
 * and returns the component sent to this audience. By default it uses [ComponentBuilder.join].
 * Supply a custom joiner to insert separators or choose another supported join configuration.
 *
 * ```kotlin
 * player.send(joiner = { it.join(" | ") }) {
 *   +"Lobby"
 *   +"Ready"
 * }
 * ```
 *
 * @param joiner transforms the populated builder into the message to send.
 * @param consumer appends the message parts before [joiner] is invoked.
 */
fun Audience.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) =
  sendMessage(component(joiner, consumer))

/**
 * Builds and joins a separate DSL message for each audience in this collection.
 *
 * For each entry, [consumer] populates a new builder and [joiner] converts it into the component
 * to send. Both callbacks run once per entry in iteration order, so neither should be assumed
 * to run only once for a broadcast. Empty collections invoke neither callback; duplicate
 * audiences receive repeated messages. The default joiner is [ComponentBuilder.join].
 *
 * ```kotlin
 * audiences.send(joiner = { it.join(" / ") }) {
 *   +"Round 1"
 *   +"Starting"
 * }
 * ```
 *
 * @param joiner produces the final component from each populated builder.
 * @param consumer builds the message parts separately for each collection entry.
 */
fun Collection<Audience>.send(joiner: (ComponentBuilder) -> Component = ComponentBuilder::join, consumer: ComponentBuilder.() -> Unit) {
  val component = component(joiner, consumer)
  forEach { it.sendMessage(component) }
}


/**
 * Builds a component with the DSL and sends it to this audience's action bar.
 *
 * [consumer] executes immediately on a fresh [ComponentBuilder], and the joined result is sent
 * through [Audience.sendActionBar] instead of ordinary chat. This helper sends a single update;
 * it does not schedule refreshes or control how long the client keeps the action bar visible.
 *
 * ```kotlin
 * player.actionBar {
 *   +"Energy: "
 *   +"100%"
 * }
 * ```
 *
 * @param consumer builds the action-bar contents once for this send.
 */
fun Audience.actionBar(consumer: ComponentBuilder.() -> Unit) = sendActionBar(component(consumer))

/**
 * Builds and sends a one-time action-bar update for each audience in this collection.
 *
 * Each entry gets a fresh component builder and a separate invocation of [consumer], following
 * iteration order. The block has a [ComponentBuilder] receiver, not an audience receiver.
 * No refresh task is created. Empty collections do nothing, and duplicate entries are sent
 * repeated updates just like separate calls to the single-audience overload.
 *
 * ```kotlin
 * audiences.actionBar {
 *   +"Next round in 10 seconds"
 * }
 * ```
 *
 * @param consumer builds the contents independently for every collection entry.
 */
fun Collection<Audience>.actionBar(consumer: ComponentBuilder.() -> Unit) {
  val component = component(consumer)
  forEach { it.sendActionBar(component) }
}

internal fun toDuration(seconds: Double): Duration {
  require(seconds.isFinite() && seconds >= 0.0) { "Seconds must be finite and non-negative" }
  return Duration.ofMillis((seconds * 1000).toLong())
}

internal fun fromDuration(duration: Duration) = duration.toMillis() / 1000.0

/**
 * Displays a title and subtitle, using seconds for the fade-in, stay, and fade-out durations.
 *
 * Durations default to the corresponding [Title.DEFAULT_TIMES] values. Each supplied duration must
 * be finite and non-negative. Fractional seconds are converted to whole milliseconds by truncation.
 * Both component blocks execute once on separate fresh builders before the title is displayed.
 * This overload replaces the title and subtitle together; use [updateTitle] to change only one part.
 *
 * ```kotlin
 * player.title(
 *   fadeInSeconds = 0.5,
 *   staySeconds = 3.0,
 *   fadeOutSeconds = 1.0,
 *   title = { +"Victory!" },
 *   subtitle = { +"You won the round." }
 * )
 * ```
 *
 * @param fadeInSeconds time in seconds for the title to fade into view.
 * @param staySeconds time in seconds to keep the title fully visible.
 * @param fadeOutSeconds time in seconds for the title to fade out.
 * @param title builds the main title component.
 * @param subtitle builds the subtitle component; use an empty block for empty contents.
 * @throws IllegalArgumentException if a duration is negative, infinite, or NaN.
 */
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

/**
 * Builds and displays a title and subtitle for every audience using shared second-based timings.
 *
 * For each collection entry, both component blocks run on fresh builders and the resulting title
 * is displayed with the supplied durations. Defaults come from [Title.DEFAULT_TIMES]; supplied
 * durations must be finite and non-negative and are truncated to whole milliseconds.
 * Iteration is sequential, duplicates are processed repeatedly, and an empty collection neither
 * builds components nor validates durations. A failure stops processing the remaining entries.
 *
 * ```kotlin
 * audiences.title(
 *   staySeconds = 5.0,
 *   title = { +"Round complete" },
 *   subtitle = { +"Returning to the lobby" }
 * )
 * ```
 *
 * @param fadeInSeconds fade-in duration in seconds for every recipient.
 * @param staySeconds fully visible duration in seconds for every recipient.
 * @param fadeOutSeconds fade-out duration in seconds for every recipient.
 * @param title builds the main title separately for each entry.
 * @param subtitle builds the subtitle separately for each entry.
 * @throws IllegalArgumentException if a duration is invalid when an entry is processed.
 */
fun Collection<Audience>.title(
  fadeInSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeIn()),
  staySeconds: Double = fromDuration(Title.DEFAULT_TIMES.stay()),
  fadeOutSeconds: Double = fromDuration(Title.DEFAULT_TIMES.fadeOut()),
  title: ComponentBuilder.() -> Unit,
  subtitle: ComponentBuilder.() -> Unit
) {
  val create = createTitle {
    title(title)
    subtitle(subtitle)
    this.fadeInSeconds = fadeInSeconds
    this.staySeconds = staySeconds
    this.fadeOutSeconds = fadeOutSeconds
  }
  forEach {
    it.showTitle(create)
  }
}

/**
 * Configures a title with the [TitleBuilder] DSL and immediately displays it to this audience.
 *
 * Set timing properties and call [TitleBuilder.title] or [TitleBuilder.subtitle] inside [builder].
 * Unspecified components are empty and unspecified timings use [Title.DEFAULT_TIMES]. For each
 * timing phase, an explicitly set tick value takes precedence over its seconds counterpart.
 * The block runs once and is not retained for later updates.
 *
 * ```kotlin
 * player.title {
 *   fadeIn = 10L
 *   staySeconds = 3.0
 *   fadeOut = 20L
 *   title { +"Welcome!" }
 *   subtitle { +"Enjoy your stay." }
 * }
 * ```
 *
 * @param builder configures the components and durations of the title to display.
 * @throws IllegalArgumentException if a selected timing is negative or non-finite, except unset sentinels.
 */
fun Audience.title(
  builder: TitleBuilder.() -> Unit
) {
  val title = createTitle(builder)
  showTitle(title)
}

/**
 * Configures and displays an independent DSL title for every audience in this collection.
 *
 * [builder] runs once per entry on a fresh [TitleBuilder], including separate execution of nested
 * component blocks. Defaults and tick-over-second precedence match the single-audience overload.
 * Captured side effects repeat for each entry; the block's receiver is not the recipient.
 * Empty collections do not execute the block, and duplicate entries are processed repeatedly.
 *
 * ```kotlin
 * audiences.title {
 *   stay = 60L
 *   title { +"Get ready!" }
 *   subtitle { +"The next round begins soon." }
 * }
 * ```
 *
 * @param builder configures a fresh title separately for each collection entry.
 * @throws IllegalArgumentException if a selected timing is invalid for a processed entry.
 */
fun Collection<Audience>.title(
  builder: TitleBuilder.() -> Unit
) {
  val title = createTitle(builder)
  forEach { it.showTitle(title) }
}

/**
 * Creates an Adventure [Title] with the title DSL without displaying it to any audience.
 *
 * [builder] executes once on a fresh [TitleBuilder]. Missing title or subtitle components are
 * empty; missing durations use [Title.DEFAULT_TIMES]. A set tick duration overrides the seconds
 * duration for the same phase. Keep the returned title when the same built contents should be
 * reused instead of executing a collection DSL block separately for every recipient.
 *
 * ```kotlin
 * val announcement = createTitle {
 *   staySeconds = 4.0
 *   title { +"Server restart" }
 *   subtitle { +"Please reconnect shortly." }
 * }
 * player.showTitle(announcement)
 * ```
 *
 * @param builder configures title contents and timing values before construction.
 * @return the constructed title, ready to pass to [Audience.showTitle].
 * @throws IllegalArgumentException if a selected timing is negative or non-finite, except unset sentinels.
 */
fun createTitle(builder: TitleBuilder.() -> Unit): Title = TitleBuilder.Companion.Builder().apply(builder).build()

/**
 * Updates selected parts of this audience's title without sending a complete replacement title.
 *
 * Only non-null arguments are sent, in title, subtitle, then timing order. A null value leaves
 * that part untouched; it does not clear it. Pass [Component.empty] to replace textual contents
 * with an empty component. Calling this method with no arguments sends nothing. These are
 * individual part updates rather than a single atomic title operation.
 *
 * ```kotlin
 * player.updateTitle(subtitle = Component.text("Starting now"))
 * ```
 *
 * @param title replacement main title, or null to leave it unchanged.
 * @param subtitle replacement subtitle, or null to leave it unchanged.
 * @param times replacement fade-in, stay, and fade-out settings, or null to leave them unchanged.
 */
fun Audience.updateTitle(
  title: Component? = null,
  subtitle: Component? = null,
  times: Title.Times? = null
) {
  title?.let { sendTitlePart(TitlePart.TITLE, it) }
  subtitle?.let { sendTitlePart(TitlePart.SUBTITLE, it) }
  times?.let { sendTitlePart(TitlePart.TIMES, it) }
}

/**
 * Applies the same partial title update to every audience in this collection.
 *
 * Non-null components and timings are reused for each entry; null parts are left untouched.
 * To clear only textual contents, supply [Component.empty] for the relevant part instead of
 * null. Entries are processed in iteration order without deduplication. An empty collection,
 * or a call with all arguments null, sends no title parts.
 *
 * ```kotlin
 * audiences.updateTitle(title = Component.text("Overtime!"))
 * ```
 *
 * @param title main title shared by all recipients, or null to preserve their existing titles.
 * @param subtitle subtitle shared by all recipients, or null to preserve existing subtitles.
 * @param times timing settings shared by all recipients, or null to preserve existing settings.
 */
fun Collection<Audience>.updateTitle(
  title: Component? = null,
  subtitle: Component? = null,
  times: Title.Times? = null
) = forEach { it.updateTitle(title, subtitle, times) }

/**
 * Clears the currently displayed title for each audience without resetting title settings.
 *
 * Delegates to [Audience.clearTitle] in collection iteration order. Use [resetTitle] instead
 * when title state and timings should also be reset. This helper does not schedule any future
 * updates, does nothing for an empty collection, and processes duplicate entries repeatedly.
 *
 * ```kotlin
 * audiences.clearTitle()
 * ```
 */
fun Collection<Audience>.clearTitle() = forEach { it.clearTitle() }

/**
 * Resets title state and settings for every audience in this collection.
 *
 * Delegates to [Audience.resetTitle], clearing the visible title and restoring default title
 * settings rather than merely hiding the current display. Use [clearTitle] when settings should
 * be preserved. Iteration follows collection order, empty collections do nothing, and duplicate
 * entries receive repeated reset calls.
 *
 * ```kotlin
 * audiences.resetTitle()
 * ```
 */
fun Collection<Audience>.resetTitle() = forEach { it.resetTitle() }


/**
 * DSL receiver for configuring title components and their display durations.
 *
 * Use through [createTitle] or the audience `title` helpers rather than constructing an
 * implementation directly. Components default to empty. Each timing phase defaults to
 * [Title.DEFAULT_TIMES] when both its tick and seconds properties are unset (`-1` and `-1.0`).
 * A configured tick value takes precedence over seconds; one tick represents 50 milliseconds.
 * Selected durations are validated when the enclosing title is built, not on property assignment.
 *
 * ```kotlin
 * val result = createTitle {
 *   fadeIn = 10L
 *   staySeconds = 2.5
 *   fadeOut = 10L
 *   title { +"Achievement unlocked" }
 *   subtitle { +"First steps" }
 * }
 * ```
 */
@TitleDsl
interface TitleBuilder {
  /**
   * Fade-in duration in ticks; `-1` leaves this value unset. When set, this takes precedence over
   * [fadeInSeconds]. If both values are unset, the default fade-in duration is used. A configured
   * value must be non-negative.
   */
  var fadeIn: Long

  /**
   * Fade-in duration in seconds; `-1.0` leaves this value unset. If [fadeIn] is also unset, the
   * default fade-in duration is used. A configured value must be finite and non-negative.
   */
  var fadeInSeconds: Double

  /**
   * Stay duration in ticks; `-1` leaves this value unset. When set, this takes precedence over
   * [staySeconds]. If both values are unset, the default stay duration is used. A configured value
   * must be non-negative.
   */
  var stay: Long

  /**
   * Stay duration in seconds; `-1.0` leaves this value unset. If [stay] is also unset, the default
   * stay duration is used. A configured value must be finite and non-negative.
   */
  var staySeconds: Double

  /**
   * Fade-out duration in ticks; `-1` leaves this value unset. When set, this takes precedence over
   * [fadeOutSeconds]. If both values are unset, the default fade-out duration is used. A configured
   * value must be non-negative.
   */
  var fadeOut: Long

  /**
   * Fade-out duration in seconds; `-1.0` leaves this value unset. If [fadeOut] is also unset, the
   * default fade-out duration is used. A configured value must be finite and non-negative.
   */
  var fadeOutSeconds: Double

  /**
   * Builds and replaces the main title component using the component DSL.
   *
   * [consumer] executes immediately on a fresh [ComponentBuilder]. Repeated calls replace the
   * previously built main title rather than appending to it, and do not change the subtitle or
   * timings. If this method is never called, the main title remains [Component.empty]. An empty
   * block can also be used to explicitly replace previously configured main title contents.
   *
   * ```kotlin
   * val result = createTitle {
   *   title {
   *     +"Welcome, "
   *     +"traveler!"
   *   }
   * }
   * ```
   *
   * @param consumer builds the complete replacement main title component.
   */
  fun title(consumer: ComponentBuilder.() -> Unit)

  /**
   * Builds and replaces the subtitle component using the component DSL.
   *
   * [consumer] executes immediately on a fresh [ComponentBuilder], independently of the main
   * title builder. Repeated calls replace the earlier subtitle rather than appending to it.
   * The main title and timings are unaffected. If omitted, the subtitle is [Component.empty];
   * use an empty block to explicitly replace previously configured subtitle contents.
   *
   * ```kotlin
   * val result = createTitle {
   *   title { +"Quest accepted" }
   *   subtitle {
   *     +"Find the hidden treasure."
   *   }
   * }
   * ```
   *
   * @param consumer builds the complete replacement subtitle component.
   */
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
