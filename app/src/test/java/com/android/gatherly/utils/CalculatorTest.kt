package com.android.gatherly.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorTest {
  private val calculator = Calculator()

  @Test
  fun testAdd() {
    assertEquals(5, calculator.add(2, 3))
    assertEquals(0, calculator.add(-1, 1))
  }

  @Test
  fun testSubtract() {
    assertEquals(1, calculator.subtract(3, 2))
    assertEquals(-2, calculator.subtract(0, 2))
  }

  @Test
  fun testMultiply() {
    assertEquals(6, calculator.multiply(2, 3))
    assertEquals(0, calculator.multiply(5, 0))
  }

  @Test
  fun testDivide() {
    assertEquals(2, calculator.divide(6, 3))
    assertEquals(5, calculator.divide(10, 2))
  }

  @Test(expected = IllegalArgumentException::class)
  fun testDivideByZero() {
    calculator.divide(5, 0)
  }
}
