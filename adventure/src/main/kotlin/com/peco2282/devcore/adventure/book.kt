package com.peco2282.devcore.adventure

import com.peco2282.devcore.adventure.builder.ComponentBuilder
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component

class BookBuilder {
  var title: Component = Component.text("Book")
  var author: Component = Component.empty()
  private val pages = mutableListOf<Component>()

  fun title(value: String) { title = Component.text(value) }
  fun author(value: String) { author = Component.text(value) }
  fun page(block: ComponentBuilder.() -> Unit) { pages += component(block) }
  fun page(component: Component) { pages += component }
  fun <T> pages(values: Iterable<T>, render: ComponentBuilder.(T) -> Unit) {
    values.forEach { value -> pages += component { render(value) } }
  }

  internal fun build(): Book = Book.book(title, author, pages.ifEmpty { listOf(Component.empty()) })
}

fun book(block: BookBuilder.() -> Unit): Book = BookBuilder().apply(block).build()
fun Audience.openBook(block: BookBuilder.() -> Unit) = openBook(book(block))
