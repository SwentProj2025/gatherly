package com.android.gatherly.utils

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RetryRule(private val retryCount: Int = 2) : TestRule {
  override fun apply(base: Statement, description: Description): Statement {
    return object : Statement() {
      override fun evaluate() {
        var caughtThrowable: Throwable? = null
        for (i in 0..retryCount) {
          try {
            base.evaluate()
            return
          } catch (t: Throwable) {
            caughtThrowable = t
            System.err.println("${description.displayName}: run ${i + 1} failed")
          }
        }
        System.err.println("${description.displayName}: giving up after ${retryCount + 1} runs")
        throw caughtThrowable!!
      }
    }
  }
}
