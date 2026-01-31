package com.peco2282.devcore.sheduler

interface Runner {
  infix fun run(task: () -> Unit): TaskHandle
}
