package com.peco2282.devcore.teleport

/** Why a teleport warmup did not complete. */
enum class CancelReason {
  MOVED,
  DAMAGED,
  QUIT,
  COOLDOWN,
  NO_SAFE_LOCATION,
}

/** Current terminal or non-terminal state of a teleport warmup. */
enum class WarmupStatus { ACTIVE, COMPLETED, CANCELLED }

/**
 * Framework-independent state machine used by the Bukkit warmup adapter.
 *
 * [movementTolerance] is measured in blocks. [moved] accepts squared distance so callers can
 * avoid a square root on every movement event.
 */
class WarmupState(
  totalTicks: Int,
  movementTolerance: Double,
) {
  init {
    require(totalTicks > 0) { "Total ticks must be positive" }
    require(movementTolerance.isFinite() && movementTolerance >= 0.0) {
      "Movement tolerance must be finite and non-negative"
    }
  }

  private var remaining = totalTicks
  private val movementToleranceSquared = movementTolerance * movementTolerance

  var status: WarmupStatus = WarmupStatus.ACTIVE
    private set

  var cancelReason: CancelReason? = null
    private set

  /** Advances one tick and returns the number of ticks remaining. */
  fun tick(): Int {
    if (status != WarmupStatus.ACTIVE) return remaining
    remaining--
    if (remaining == 0) status = WarmupStatus.COMPLETED
    return remaining
  }

  /** Cancels when [distanceSquared] exceeds the configured movement tolerance. */
  fun moved(distanceSquared: Double): Boolean =
    distanceSquared > movementToleranceSquared && cancel(CancelReason.MOVED)

  /** Cancels the active warmup. The first terminal outcome always wins. */
  fun cancel(reason: CancelReason): Boolean {
    if (status != WarmupStatus.ACTIVE) return false
    status = WarmupStatus.CANCELLED
    cancelReason = reason
    return true
  }
}
