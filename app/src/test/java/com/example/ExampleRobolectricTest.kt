package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("إتقان الوقت", appName)
  }

  @Test
  fun `verify curriculum units count and quiz questions`() {
    val units = com.example.data.model.LearningCurriculum.units
    assertEquals(5, units.size)
    units.forEach { unit ->
      assertEquals(3, unit.questions.size)
    }
  }
}
