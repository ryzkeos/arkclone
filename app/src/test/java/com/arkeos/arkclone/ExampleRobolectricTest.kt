package com.arkeos.arkclone

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.arkeos.arkclone.data.CloneStorage
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.ClonedInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ark Clone", appName)
  }

  @Test
  fun `verify initial clone storage starts empty upon installation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = CloneStorage(context)
    val instances = storage.loadInstances()

    assertNotNull(instances)
    // As per user specification: no auto-cloned apps upon installation
    assertTrue("Should start with empty instances list upon install", instances.isEmpty())
  }

  @Test
  fun `verify saving and loading cloned instances`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = CloneStorage(context)

    val testInstance = ClonedInstance(
      id = "test_1",
      appName = "Chrome",
      packageName = "com.android.chrome",
      cloneNumber = 1,
      alias = "Work Chrome",
      badgeTag = "WORK",
      category = AppCategory.TOOLS,
      realSourceApkPath = "/data/app/chrome.apk"
    )

    storage.saveInstances(listOf(testInstance))
    val loaded = storage.loadInstances()

    assertEquals(1, loaded.size)
    assertEquals("Chrome", loaded[0].appName)
    assertEquals("/data/app/chrome.apk", loaded[0].realSourceApkPath)
  }

  @Test
  fun `verify dark mode toggle setting`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = CloneStorage(context)
    assertTrue("Default should be dark mode", storage.isDarkMode())

    storage.setDarkMode(false)
    assertEquals(false, storage.isDarkMode())
  }
}
