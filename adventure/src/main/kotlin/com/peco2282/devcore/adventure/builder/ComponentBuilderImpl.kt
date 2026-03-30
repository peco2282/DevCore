package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.ComponentDsl
import com.peco2282.devcore.adventure.ExperimentalNbtComponent
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.style
import com.peco2282.devcore.adventure.updateLast
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.*
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.ApiStatus

@ComponentDsl
internal class ComponentBuilderImpl : ComponentBuilder {
  private val components = mutableListOf<Component>()

  override fun mini(content: String, vararg tags: TagResolver): ComponentBuilderImpl =
    apply {
      components.add(MiniMessage.miniMessage().deserialize(content, *tags))
    }

  override infix fun append(content: String): ComponentBuilderImpl =
    apply {
      components.add(content.component())
    }

  override infix fun append(component: Component): ComponentBuilderImpl =
    apply {
      components.add(component)
    }

  override infix fun create(consumer: ComponentBuilder.() -> Unit): ComponentBuilderImpl =
    apply {
      components.add(component(consumer))
    }

//  override infix fun withStyle(consumer: Styler.() -> Unit): ComponentBuilderImpl =
//    apply {
//      components.updateLast {
//        val style = StyleBuilder().apply(consumer).build()
//        it.style(style)
//      }
//    }

  override fun join(): Component =
    join(Component.empty())

  override fun join(sep: String): Component =
    join(sep.convert)

  override fun join(sep: Component): Component =
    join(JoinConfiguration.separator(sep))

  override fun join(conf: JoinConfiguration): Component =
    Component.join(conf, components)

  override fun collect(): Component =
    collect(Component.empty())

  override fun collect(separator: String): Component =
    collect(separator.convert)

  override fun collect(separator: Component): Component =
    components.stream().collect(Component.toComponent(separator))

  override fun styleLast(style: Style): ComponentBuilder = apply {
    components.updateLast { it.style(style) }
  }

  override fun selector(key: String): ComponentBuilder =
    apply {
      append(Component.selector(key))
    }

  override fun insertion(text: String): ComponentBuilder =
    apply { components.updateLast { it.insertion(text) } }

  override fun hoverEvent(event: HoverEvent<*>): ComponentBuilder =
    apply { components.updateLast { it.hoverEvent(event) } }

  override fun newline(): ComponentBuilder =
    apply { append(Component.newline()) }

  override fun space(): ComponentBuilder =
    apply { append(Component.space()) }

  override fun text(content: String): ComponentBuilder =
    apply {
      append(content.component())
    }

  override fun text(content: String, consumer: Styler.() -> Unit): ComponentBuilder =
    apply {
      val styler = StyleBuilder().apply(consumer)
      val gradientImpl = styler.getGradient() as? GradientImpl
      if (gradientImpl != null && gradientImpl.colors.size >= 2) {
        val colors = gradientImpl.colors
        val weights = if (gradientImpl.weights.size == colors.size) {
          gradientImpl.weights
        } else {
          List(colors.size) { 1.0 }
        }

        val totalWeight = weights.sum()
        val cumulativeWeights = mutableListOf<Double>()
        var currentSum = 0.0
        weights.forEach {
          currentSum += it
          cumulativeWeights.add(currentSum)
        }

        val textBuilder = Component.text()
        val style = styler.build()

        for (i in content.indices) {
          val progress = if (content.length > 1) {
            (i.toDouble() / (content.length - 1) + gradientImpl.phase)
          } else {
            gradientImpl.phase.toDouble()
          }
          // Wrap progress to [0, 1] range, but keep 1.0 as 1.0 (not 0.0) if it was exactly 1.0 initially (phase 0, last char)
          val wrappedProgress = when {
            progress < 0.0 -> (progress % 1.0 + 1.0) % 1.0
            progress >= 1.0 -> if (progress % 1.0 == 0.0) 1.0 else progress % 1.0
            else -> progress
          }
          val weightedProgress = wrappedProgress * totalWeight

          val colorIndex = cumulativeWeights.indexOfFirst { it >= weightedProgress }.coerceIn(0, colors.size - 1)

          val color = if (colorIndex == 0) {
            val p = (weightedProgress / cumulativeWeights[0]).toFloat().coerceIn(0f, 1f)
            interpolate(colors.first(), colors[colorIndex], p) // colorIndex is 0, so interpolate(colors[0], colors[0], p) which is colors[0]
          } else {
            val p = ((weightedProgress - cumulativeWeights[colorIndex - 1]) / (cumulativeWeights[colorIndex] - cumulativeWeights[colorIndex - 1])).toFloat().coerceIn(0f, 1f)
            interpolate(colors[colorIndex - 1], colors[colorIndex], p)
          }

          textBuilder.append(content[i].toString().component(style.color(color)))
        }
        append(textBuilder.build())
      } else {
        append(content.component(styler.build()))
      }
    }

  private fun interpolate(from: TextColor, to: TextColor, ratio: Float): TextColor {
    val r = (from.red() + (to.red() - from.red()) * ratio).toInt().coerceIn(0, 255)
    val g = (from.green() + (to.green() - from.green()) * ratio).toInt().coerceIn(0, 255)
    val b = (from.blue() + (to.blue() - from.blue()) * ratio).toInt().coerceIn(0, 255)
    return TextColor.color(r, g, b)
  }

  override fun whenTrue(
    condition: Boolean,
    consumer: ComponentBuilder.() -> Unit
  ): ComponentBuilder =
    apply { if (condition) append(component(consumer)) }

  override fun translatable(
    key: String,
    vararg args: Component
  ): ComponentBuilder =
    apply { append(Component.translatable(key, *args)) }

  override fun translatableAny(key: String, vararg args: Any): ComponentBuilder =
    apply {
      val components = args.map {
        when (it) {
          is Component -> it
          else -> it.toString().convert
        }
      }
      append(Component.translatable(key, components))
    }

  override fun translatable(
    key: String,
    args: List<Component>
  ): ComponentBuilder =
    apply { append(Component.translatable(key, args)) }

  override fun translatable(
    key: String,
    consumer: ComponentBuilder.() -> Unit
  ): ComponentBuilder =
    apply {
      val builder = ComponentBuilderImpl().apply(consumer)
      append(Component.translatable(key, builder.components))
    }

  override fun translatable(
    key: String,
    style: Style
  ): ComponentBuilder =
    apply {
      append(Component.translatable(key, style))
    }

  override fun translatable(
    key: String,
    style: Style,
    vararg args: Component
  ): ComponentBuilder =
    apply {
      append(Component.translatable(key, style, *args))
    }

  override fun translatable(
    key: String,
    styler: Styler.() -> Unit,
    args: List<Component>
  ): ComponentBuilder =
    apply {
      val style = style(styler)
      append(Component.translatable(key, style, args))
    }

  override fun translatable(
    key: String,
    styler: Styler.() -> Unit,
    consumer: ComponentBuilder.() -> Unit
  ): ComponentBuilder =
    apply {
      val style = style(styler)
      val builder = ComponentBuilderImpl().apply(consumer)
      append(Component.translatable(key, style, builder.components))
    }

  override fun keybind(key: String): ComponentBuilder =
    apply { append(Component.keybind(key)) }

  override fun score(name: String, objective: String): ComponentBuilder =
    apply { append(Component.score(name, objective)) }

  override fun <T> forEach(iterable: Iterable<T>, action: ComponentBuilder.(T) -> Unit): ComponentBuilder =
    apply { iterable.forEach { action(it) } }

  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  override fun blockNbt(
    @Language("NBTPath") nbt: String,
    consumer: BlockNBTComponent.Builder.() -> Unit
  ): ComponentBuilder =
    apply { append(Component.blockNBT().nbtPath(nbt).apply(consumer).build()) }

  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  override fun entityNbt(
    @Language("NBTPath") nbt: String,
    consumer: EntityNBTComponent.Builder.() -> Unit
  ): ComponentBuilder =
    apply { append(Component.entityNBT().nbtPath(nbt).apply(consumer).build()) }

  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  override fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: Key,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): ComponentBuilder =
    apply { append(Component.storageNBT().nbtPath(nbt).storage(storage).apply(consumer).build()) }

  @ApiStatus.Experimental
  @ExperimentalNbtComponent
  override fun storageNbt(
    @Language("NBTPath") nbt: String,
    storage: String,
    consumer: StorageNBTComponent.Builder.() -> Unit
  ): ComponentBuilder =
    storageNbt(nbt, Key.key(storage), consumer)
}
