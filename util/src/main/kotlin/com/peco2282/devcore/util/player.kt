package com.peco2282.devcore.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import java.time.Duration

/**
 * Plays a sound to the player at their current location.
 *
 * This is a convenient extension function that simplifies playing sounds to players
 * by automatically using the player's current location and providing sensible defaults
 * for common parameters.
 *
 * @param sound The sound effect to play. Use values from the [Sound] enum.
 * @param volume The volume at which to play the sound. Default is 1.0 (100%).
 *               Values greater than 1.0 will be louder, values less than 1.0 will be quieter.
 *               The volume affects how far away the sound can be heard.
 * @param pitch The pitch at which to play the sound. Default is 1.0 (normal pitch).
 *              Values range from 0.5 (lower pitch) to 2.0 (higher pitch).
 *              Affects the speed and tone of the sound.
 * @param category The sound category this sound belongs to. Default is [SoundCategory.MASTER].
 *                 This determines which volume slider in the client's settings affects this sound.
 *
 * @receiver The player who will hear the sound.
 *
 * @see Sound for available sound effects
 * @see SoundCategory for available sound categories
 *
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * // Play a simple click sound
 * player.playSound(Sound.UI_BUTTON_CLICK)
 *
 * // Play a louder explosion sound
 * player.playSound(Sound.ENTITY_GENERIC_EXPLODE, volume = 2.0f)
 *
 * // Play a high-pitched ding sound in the records category
 * player.playSound(
 *     sound = Sound.BLOCK_NOTE_BLOCK_PLING,
 *     pitch = 2.0f,
 *     category = SoundCategory.RECORDS
 * )
 * ```
 */
fun Player.playSound(
  sound: Sound,
  volume: Float = 1f,
  pitch: Float = 1f,
  category: SoundCategory = SoundCategory.MASTER
) {
  playSound(location, sound, category, volume, pitch)
}

/**
 * Displays a title and/or subtitle to the player with customizable timing.
 *
 * This extension function provides a convenient way to show titles to players using
 * the Adventure API's Component system, with full control over timing and animation.
 *
 * @param title The main title to display. Default is an empty component (no title).
 *              Use [Component] factory methods to create styled text.
 * @param subtitle The subtitle to display below the main title. Default is an empty component.
 *                 Subtitles are typically smaller and appear below the main title.
 * @param fadeIn The duration for the title to fade in. Default is 500 milliseconds.
 *               This controls how long the title takes to appear on screen.
 * @param stay The duration for the title to remain fully visible. Default is 3 seconds.
 *             This is how long the title stays at full opacity after fading in.
 * @param fadeOut The duration for the title to fade out. Default is 500 milliseconds.
 *                This controls how long the title takes to disappear from screen.
 *
 * @receiver The player who will see the title.
 *
 * @see Component for creating styled text
 * @see Title for more title options
 * @see Duration for time specifications
 *
 * @since 1.0.0
 *
 * Example usage:
 * ```kotlin
 * // Show a simple title
 * player.showTitle(title = Component.text("Welcome!"))
 *
 * // Show title with subtitle
 * player.showTitle(
 *     title = Component.text("Game Started", NamedTextColor.GOLD),
 *     subtitle = Component.text("Good luck!", NamedTextColor.GRAY)
 * )
 *
 * // Show title with custom timing (quick flash)
 * player.showTitle(
 *     title = Component.text("Alert!"),
 *     fadeIn = Duration.ofMillis(100),
 *     stay = Duration.ofMillis(500),
 *     fadeOut = Duration.ofMillis(100)
 * )
 *
 * // Show only subtitle with longer display time
 * player.showTitle(
 *     subtitle = Component.text("Check your inventory"),
 *     stay = Duration.ofSeconds(5)
 * )
 * ```
 */
fun Player.showTitle(
  title: Component = Component.empty(),
  subtitle: Component = Component.empty(),
  fadeIn: Duration = Duration.ofMillis(500),
  stay: Duration = Duration.ofSeconds(3),
  fadeOut: Duration = Duration.ofMillis(500)
) {
  val times = Title.Times.times(fadeIn, stay, fadeOut)
  showTitle(Title.title(title, subtitle, times))
}
