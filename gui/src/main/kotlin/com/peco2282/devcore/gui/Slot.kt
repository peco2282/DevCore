package com.peco2282.devcore.gui

data class Slot(
  val x: Int,
  val y: Int
) {
  init {
    require(x >= 0) { "x must be non-negative" }
    require(y >= 0) { "y must be non-negative" }
  }

  fun slot(): Int = x * 9 + y

  companion object {
    val SLOT_1_1 = Slot(0, 0)
    val SLOT_1_2 = Slot(0, 1)
    val SLOT_1_3 = Slot(0, 2)
    val SLOT_1_4 = Slot(0, 3)
    val SLOT_1_5 = Slot(0, 4)
    val SLOT_1_6 = Slot(0, 5)
    val SLOT_1_7 = Slot(0, 6)
    val SLOT_1_8 = Slot(0, 7)
    val SLOT_1_9 = Slot(0, 8)

    val SLOT_2_1 = Slot(1, 0)
    val SLOT_2_2 = Slot(1, 1)
    val SLOT_2_3 = Slot(1, 2)
    val SLOT_2_4 = Slot(1, 3)
    val SLOT_2_5 = Slot(1, 4)
    val SLOT_2_6 = Slot(1, 5)
    val SLOT_2_7 = Slot(1, 6)
    val SLOT_2_8 = Slot(1, 7)
    val SLOT_2_9 = Slot(1, 8)

    val SLOT_3_1 = Slot(2, 0)
    val SLOT_3_2 = Slot(2, 1)
    val SLOT_3_3 = Slot(2, 2)
    val SLOT_3_4 = Slot(2, 3)
    val SLOT_3_5 = Slot(2, 4)
    val SLOT_3_6 = Slot(2, 5)
    val SLOT_3_7 = Slot(2, 6)
    val SLOT_3_8 = Slot(2, 7)
    val SLOT_3_9 = Slot(2, 8)

    val SLOT_4_1 = Slot(3, 0)
    val SLOT_4_2 = Slot(3, 1)
    val SLOT_4_3 = Slot(3, 2)
    val SLOT_4_4 = Slot(3, 3)
    val SLOT_4_5 = Slot(3, 4)
    val SLOT_4_6 = Slot(3, 5)
    val SLOT_4_7 = Slot(3, 6)
    val SLOT_4_8 = Slot(3, 7)
    val SLOT_4_9 = Slot(3, 8)

    val SLOT_5_1 = Slot(4, 0)
    val SLOT_5_2 = Slot(4, 1)
    val SLOT_5_3 = Slot(4, 2)
    val SLOT_5_4 = Slot(4, 3)
    val SLOT_5_5 = Slot(4, 4)
    val SLOT_5_6 = Slot(4, 5)
    val SLOT_5_7 = Slot(4, 6)
    val SLOT_5_8 = Slot(4, 7)
    val SLOT_5_9 = Slot(4, 8)

    val SLOT_6_1 = Slot(5, 0)
    val SLOT_6_2 = Slot(5, 1)
    val SLOT_6_3 = Slot(5, 2)
    val SLOT_6_4 = Slot(5, 3)
    val SLOT_6_5 = Slot(5, 4)
    val SLOT_6_6 = Slot(5, 5)
    val SLOT_6_7 = Slot(5, 6)
    val SLOT_6_8 = Slot(5, 7)
    val SLOT_6_9 = Slot(5, 8)
  }
}
