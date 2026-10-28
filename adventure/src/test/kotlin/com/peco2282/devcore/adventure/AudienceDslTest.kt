package com.peco2282.devcore.adventure

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.time.Duration
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AudienceDslTest {
  @Test
  fun `duration conversion preserves seconds`() {
    assertEquals(Duration.ofMillis(1_500), toDuration(1.5))
    assertEquals(3.0, fromDuration(Duration.ofSeconds(3)))
  }

  @Test
  fun `duration conversion rejects invalid seconds`() {
    assertThrows(IllegalArgumentException::class.java) { toDuration(-0.1) }
    assertThrows(IllegalArgumentException::class.java) { toDuration(Double.NaN) }
    assertThrows(IllegalArgumentException::class.java) { toDuration(Double.POSITIVE_INFINITY) }
  }

  @Test
  fun `title builder defaults to Adventure times`() {
    val built = createTitle {
      title { text("Title") }
    }

    assertEquals(Title.DEFAULT_TIMES, built.times())
    assertEquals(Component.text("Title"), built.title())
    assertEquals(Component.empty(), built.subtitle())
  }

  @Test
  fun `component title overload supports title only`() {
    val recorder = AudienceRecorder()
    val title = Component.text("Only title")

    recorder.audience.updateTitle(title)

    val call = recorder.calls.single()
    assertEquals("sendTitlePart", call.first)
    assertEquals(TitlePart.TITLE, call.second?.get(0))
    assertEquals(title, call.second?.get(1))
  }

  @Test
  fun `title partial update without parts does not send anything`() {
    val recorder = AudienceRecorder()

    recorder.audience.updateTitle()

    assertEquals(0, recorder.calls.size)
  }

  @Test
  fun `title builder supports seconds and ticks`() {
    val built = createTitle {
      fadeInSeconds = 1.5
      stay = 12
      fadeOutSeconds = 0.25
    }
    val times = built.times()!!

    assertEquals(Duration.ofMillis(1_500), times.fadeIn())
    assertEquals(Duration.ofMillis(600), times.stay())
    assertEquals(Duration.ofMillis(250), times.fadeOut())
  }

  @Test
  fun `title builder rejects invalid timing`() {
    assertThrows(IllegalArgumentException::class.java) { createTitle { fadeIn = -2 } }
    assertThrows(IllegalArgumentException::class.java) { createTitle { staySeconds = Double.NaN } }
  }

  @Test
  fun `title partial update and lifecycle operations reach audience`() {
    val recorder = AudienceRecorder()
    val second = AudienceRecorder()
    val title = Component.text("Updated")
    val audiences = listOf(recorder.audience, second.audience)

    audiences.updateTitle(title = title)
    audiences.clearTitle()
    audiences.resetTitle()

    assertEquals("sendTitlePart", recorder.calls[0].first)
    assertEquals(TitlePart.TITLE, recorder.calls[0].second?.get(0))
    assertEquals(title, recorder.calls[0].second?.get(1))
    assertEquals(listOf("clearTitle", "resetTitle"), recorder.calls.drop(1).map { it.first })
    assertEquals(recorder.calls.map { it.first }, second.calls.map { it.first })
  }

  @Test
  fun `collection title builds once and sends to each audience`() {
    val first = AudienceRecorder()
    val second = AudienceRecorder()

    listOf(first.audience, second.audience).title {
      title { text("Shared") }
    }

    val firstTitle = first.calls.single().second?.single()
    val secondTitle = second.calls.single().second?.single()
    assertSame(firstTitle, secondTitle)
  }

  @Test
  fun `boss bar builder sets configuration and update mutates existing bar`() {
    val bar = bossBar {
      name { text("Progress") }
      progress = 0.5f
      color = BossBar.Color.BLUE
      overlay = BossBar.Overlay.NOTCHED_10
      flag(BossBar.Flag.DARKEN_SCREEN)
    }

    assertEquals(Component.text("Progress"), bar.name())
    assertEquals(0.5f, bar.progress())
    assertEquals(BossBar.Color.BLUE, bar.color())
    assertEquals(BossBar.Overlay.NOTCHED_10, bar.overlay())
    assertEquals(setOf(BossBar.Flag.DARKEN_SCREEN), bar.flags())

    val updated = bar.update {
      progress = 0.75f
      flag(BossBar.Flag.DARKEN_SCREEN, false)
    }

    assertSame(bar, updated)
    assertEquals(0.75f, bar.progress())
    assertEquals(emptySet<BossBar.Flag>(), bar.flags())
  }

  @Test
  fun `boss bar rejects invalid progress`() {
    assertThrows(IllegalArgumentException::class.java) { bossBar { progress = -0.1f } }
    assertThrows(IllegalArgumentException::class.java) { bossBar { progress = Float.NaN } }
  }

  @Test
  fun `boss bar show and hide operations cover audience collections`() {
    val first = AudienceRecorder()
    val second = AudienceRecorder()
    val audiences = listOf(first.audience, second.audience)

    val bar = audiences.showBossBar {
      name { text("Shared") }
    }
    audiences.hideBossBar(bar)

    assertEquals(listOf("showBossBar", "hideBossBar"), first.calls.map { it.first })
    assertEquals(listOf("showBossBar", "hideBossBar"), second.calls.map { it.first })
    assertSame(bar, first.calls.first().second?.single())
    assertSame(bar, second.calls.first().second?.single())
  }

  private class AudienceRecorder : InvocationHandler {
    val calls = mutableListOf<Pair<String, Array<out Any?>?>>()
    val audience: Audience = Proxy.newProxyInstance(
      Audience::class.java.classLoader,
      arrayOf(Audience::class.java),
      this
    ) as Audience

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
      calls += method.name to args
      return null
    }
  }
}