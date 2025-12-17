package com.android.gatherly.model.profile

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the [UserStatusSource] enum class. */
class UserStatusSourceTest {
  /** Tests whether the fromString method correctly maps string values to enum constants. */
  @Test
  fun fromString_returnsCorrectEnum() {
    // Valid values
    assertEquals(UserStatusSource.MANUAL, UserStatusSource.fromString("manual"))
    assertEquals(UserStatusSource.AUTOMATIC, UserStatusSource.fromString("automatic"))

    // Invalid or null values default to AUTOMATIC
    assertEquals(UserStatusSource.AUTOMATIC, UserStatusSource.fromString("unknown"))
    assertEquals(UserStatusSource.AUTOMATIC, UserStatusSource.fromString(null))
  }
}
