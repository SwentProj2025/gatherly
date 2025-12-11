package com.android.gatherly.model.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class UserStatusSourceTest {
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
