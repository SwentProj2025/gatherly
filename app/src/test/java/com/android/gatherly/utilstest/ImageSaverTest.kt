package com.android.gatherly.utilstest

import android.content.Context
import android.graphics.Bitmap
import com.android.gatherly.utils.saveProfilePictureLocally
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ImageSaverTest {

  private lateinit var context: Context
  private lateinit var bitmap: Bitmap
  private lateinit var filesDir: java.io.File

  @Before
  fun setup() {
    context = Mockito.mock(Context::class.java)
    bitmap = Mockito.mock(Bitmap::class.java)

    filesDir = createTempDirectory("testFiles").toFile()
    Mockito.`when`(context.filesDir).thenReturn(filesDir)
  }

  @After
  fun cleanup() {
    filesDir.deleteRecursively()
  }

  /**
   * Verifies that saveProfilePictureLocally successfully writes the bitmap to a temporary files
   * directory and returns true when no errors occur.
   */
  @Test
  fun saveProfilePictureLocally_returnsTrue_whenSuccessful() = runTest {
    val result = saveProfilePictureLocally(context, bitmap, Dispatchers.IO)
    assertTrue(result)
  }

  /**
   * Verifies that saveProfilePictureLocally returns false if bitmap.compress throws an exception,
   * simulating a failure during saving.
   */
  @Test
  fun saveProfilePictureLocally_returnsFalse_whenBitmapCompressFails() = runTest {
    Mockito.`when`(bitmap.compress(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenThrow(RuntimeException("compress failed"))
    val result = saveProfilePictureLocally(context, bitmap, Dispatchers.IO)
    assertFalse(result)
  }
}
