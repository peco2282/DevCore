package com.peco2282.devcore.adventure.builder

import com.peco2282.devcore.adventure.ComponentDsl
import com.peco2282.devcore.adventure.component
import com.peco2282.devcore.adventure.style
import com.peco2282.devcore.adventure.updateLast
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.EntityNBTComponent
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.ApiStatus

@ComponentDsl
internal class ComponentBuilder : Componenter {
  private val components = mutableListOf<Component>()

  override infix fun append(content: String): ComponentBuilder =
    apply {
      components.add(Component.text(content))
    }

  override infix fun append(component: Component): ComponentBuilder =
    apply {
      components.add(component)
    }

  override infix fun create(consumer: Componenter.() -> Unit): ComponentBuilder =
    apply {
      components.add(component(consumer))
    }

//  override infix fun withStyle(consumer: Styler.() -> Unit): ComponentBuilder =
//    apply {
//      components.updateLast {
//        val style = StyleBuilder().apply(consumer).build()
//        it.style(style)
//      }
//    }

  override fun join(): Component =
    join(Component.text(""))

  override fun join(sep: String): Component =
    join(Component.text(sep))

  override fun join(sep: Component): Component =
    join(JoinConfiguration.separator(sep))

  override fun join(conf: JoinConfiguration): Component =
    Component.join(conf, components)

  override fun collect(): Component =
    collect(Component.text(""))

  override fun collect(separator: String): Component =
    collect(Component.text(separator))

  override fun collect(separator: Component): Component =
    components.stream().collect(Component.toComponent(separator))

  override fun styleLast(style: Style): Componenter = apply {
    components.updateLast { it.style(style) }
  }

  override fun selector(key: String): Componenter =
    apply {
      append(Component.selector(key))
    }

  override fun insertion(text: String): Componenter =
    apply { components.updateLast { it.insertion(text) } }

  override fun hoverEvent(event: HoverEvent<*>): Componenter =
    apply { components.updateLast { it.hoverEvent(event) } }

  override fun newline(): Componenter =
    apply { append(Component.newline()) }

  override fun space(): Componenter =
    apply { append(Component.space()) }

  override fun whenTrue(
    condition: Boolean,
    consumer: Componenter.() -> Unit
  ): Componenter =
    apply { if (condition) append(component(consumer)) }

  override fun translatable(
    key: String,
    vararg args: Component
  ): Componenter =
    apply { append(Component.translatable(key, *args)) }

  override fun translatable(
    key: String,
    args: List<Component>
  ): Componenter =
    apply { append(Component.translatable(key, args)) }

  override fun translatable(
    key: String,
    consumer: Componenter.() -> Unit
  ): Componenter =
    apply {
      val builder = ComponentBuilder().apply(consumer)
      append(Component.translatable(key, builder.components))
    }

  override fun translatable(
    key: String,
    style: Style
  ): Componenter =
    apply {
    append(Component.translatable(key, style))
  }

  override fun translatable(
    key: String,
    style: Style,
    vararg args: Component
  ): Componenter =
    apply {
      append(Component.translatable(key, style, *args))
  }

  override fun translatable(
    key: String,
    styler: Styler.() -> Unit,
    args: List<Component>
  ): Componenter =
    apply {
      val style = style(styler)
      append(Component.translatable(key, style, args))
    }

  override fun translatable(
    key: String,
    styler: Styler.() -> Unit,
    consumer: Componenter.() -> Unit
  ): Componenter =
    apply {
      val style = style(styler)
      val builder = ComponentBuilder().apply(consumer)
      append(Component.translatable(key, style, builder.components))
    }

  override fun keybind(key: String): Componenter =
    apply { append(Component.keybind(key)) }

  override fun score(name: String, objective: String): Componenter =
    apply { append(Component.score(name, objective)) }

  override fun <T> forEach(iterable: Iterable<T>, action: Componenter.(T) -> Unit): Componenter =
    apply { iterable.forEach { action(it) } }

  @ApiStatus.Experimental
  override fun blockNbt(
    @Language("NBTPath") nbt: String,
    consumer: BlockNBTComponent.Builder.() -> Unit
  ): Componenter =
    apply { append(Component.blockNBT().nbtPath(nbt).apply(consumer).build()) }

  @ApiStatus.Experimental
  override fun entityNbt(
    @Language("NBTPath") nbt: String,
    consumer: EntityNBTComponent.Builder.() -> Unit
  ): Componenter =
    apply { append(Component.entityNBT().nbtPath(nbt).apply(consumer).build()) }
}
