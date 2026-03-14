package com.peco2282.devcore.gui

import net.kyori.adventure.text.Component

/**
 * A context class for GUIs that support pagination.
 *
 * @param rows The number of rows in the GUI.
 */
@GuiDsl
abstract class PaginatedGuiContext<T>(rows: Int) : GuiContext(rows) {
  /**
   * The list of items to be paginated.
   */
  var items: List<T> by state(emptyList())

  /**
   * The current page index (0-indexed).
   */
  var currentPage: Int by state(0)

  /**
   * The number of items to display per page.
   */
  var pageSize: Int = (rows - 1) * 9

  /**
   * The total number of pages.
   */
  val totalPages: Int
    get() = if (items.isEmpty()) 1 else kotlin.math.ceil(items.size.toDouble() / pageSize).toInt()

  /**
   * Returns the items to be displayed on the current page.
   */
  protected fun getPageItems(): List<T> {
    val start = currentPage * pageSize
    val end = minOf(start + pageSize, items.size)
    return if (start < items.size) items.subList(start, end) else emptyList()
  }

  /**
   * Goes to the next page.
   */
  fun nextPage() {
    if (currentPage < totalPages - 1) {
      currentPage++
    }
  }

  /**
   * Goes to the previous page.
   */
  fun prevPage() {
    if (currentPage > 0) {
      currentPage--
    }
  }

  /**
   * DSL for defining how to display each item in the list.
   *
   * @param slots The slots to use for displaying items.
   * @param display The builder block for each item.
   */
  fun content(slots: List<Int>, display: SlotCreator.(T) -> Unit) {
    pageSize = slots.size
    val pageItems = getPageItems()
    pageItems.forEachIndexed { index, item ->
      if (index < slots.size) {
        slot(slots[index]) {
          display(item)
        }
      }
    }
  }

  /**
   * DSL for defining how to display each item in the list using a range of slots.
   *
   * @param range The range of slots to use.
   * @param display The builder block for each item.
   */
  fun content(range: IntRange, display: SlotCreator.(T) -> Unit) = content(range.toList(), display)
}

/**
 * Creates a paginated [Gui] instance.
 *
 * @param rows The number of rows (1-6).
 * @param title The title of the GUI.
 * @param items The initial list of items.
 * @param builder The builder block for the [PaginatedGuiContext].
 * @return A new [Gui] instance.
 */
fun <T> paginatedInventory(
  rows: Int = 6,
  title: Component,
  items: List<T> = emptyList(),
  builder: PaginatedGuiContext<T>.() -> Unit
): Gui {
  return (object : PaginatedGuiContext<T>(rows) {
    init {
      this.items = items
    }
    override fun build() {
      title(title)
      this.builder()
    }
  }).apply { update() }
}
