package com.android.gatherly.utilstest

import android.content.Context
import android.graphics.Bitmap
import com.android.gatherly.utils.saveProfilePictureLocally
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class ImageSaverTest {

  /**
   * Verifies that saveProfilePictureLocally successfully writes the bitmap to a temporary files
   * directory and returns true when no errors occur.
   */
  @Test
  fun saveProfilePictureLocally_returnsTrue_whenSuccessful() = runTest {
    val context = Mockito.mock(Context::class.java)
    val bitmap = Mockito.mock(Bitmap::class.java)

    val filesDir = createTempDirectory("testFiles").toFile()
    Mockito.`when`(context.filesDir).thenReturn(filesDir)

    val result = saveProfilePictureLocally(context, bitmap, Dispatchers.IO)
    assertTrue(result)

    filesDir.deleteRecursively()
  }

  /**
   * Verifies that saveProfilePictureLocally returns false if bitmap.compress throws an exception,
   * simulating a failure during saving.
   */
  @Test
  fun saveProfilePictureLocally_returnsFalse_whenBitmapCompressFails() = runTest {
    val context = Mockito.mock(Context::class.java)
    val bitmap = Mockito.mock(Bitmap::class.java)

    Mockito.`when`(bitmap.compress(Mockito.any(), Mockito.anyInt(), Mockito.any()))
        .thenThrow(RuntimeException("compress failed"))

    val filesDir = createTempDirectory("testFiles").toFile()
    Mockito.`when`(context.filesDir).thenReturn(filesDir)

    val result = saveProfilePictureLocally(context, bitmap, Dispatchers.IO)
    assertFalse(result)

    filesDir.deleteRecursively()
  }
}
