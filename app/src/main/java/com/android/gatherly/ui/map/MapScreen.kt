package com.android.gatherly.ui.map

import android.graphics.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.ui.navigation.BottomNavigationMenu
import com.android.gatherly.ui.navigation.HandleSignedOutState
import com.android.gatherly.ui.navigation.NavigationActions
import com.android.gatherly.ui.navigation.NavigationTestTags
import com.android.gatherly.ui.navigation.Tab
import com.android.gatherly.ui.navigation.TopNavigationMenu
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.text.SimpleDateFormat
import java.util.Locale

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the
// SwEnt staff.
// The icons were created with the help of an LLM (ChatGPT).

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  fun getTestTagForTodoMarker(todoId: String): String = "todoMarker_$todoId"
  fun getTestTagForTodoMarkerExpanded(todoId: String): String = "todoMarkerExpanded_$todoId"
}

/**
 * A composable screen displaying ToDos as interactive markers on a Google Map.
 *
 * @param viewModel The MapViewModel instance providing the list of ToDos, the current camera
 *   position, and marker interaction handlers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedOut: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {

  val uiState by viewModel.uiState.collectAsState()
  HandleSignedOutState(uiState.onSignedOut, onSignedOut)

  Scaffold(
      topBar = {
        TopNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.TOP_NAVIGATION_MENU),
            onSignedOut = { viewModel.signOut(credentialManager) })
      },
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Map,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      },
      content = { pd ->
        // Camera position state, using the first ToDo location if available
        val cameraPositionState = rememberCameraPositionState {
          position = CameraPosition.fromLatLngZoom(uiState.cameraPos, 10f)
        }
        GoogleMap(
            modifier =
                Modifier.fillMaxSize().padding(pd).testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
            cameraPositionState = cameraPositionState) {
              uiState.todoList.forEach { todo ->
                val loc = todo.location ?: return@forEach
                val isExpanded = uiState.expandedTodoId == todo.uid

                val markerTestTag =
                    if (isExpanded) MapScreenTestTags.getTestTagForTodoMarkerExpanded(todo.uid)
                    else MapScreenTestTags.getTestTagForTodoMarker(todo.uid)

                val formattedDate =
                    remember(todo.dueDate) {
                      val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                      sdf.format(todo.dueDate.toDate())
                    }

                // not clicked
                val iconCollapsed = todoIcon(todo.name)

                // clicked
                val iconExpanded =
                    todoExpanded(
                        title = todo.name,
                        description = todo.description,
                        dateText = formattedDate,
                        expanded = true)

                Marker(
                    state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                    icon = if (isExpanded) iconExpanded else iconCollapsed,
                    onClick = {
                      if (isExpanded) viewModel.onTodoMarkerDismissed()
                      else viewModel.onTodoMarkerTapped(todo.uid)
                      true
                    })
              }
            }
      })
}

/**
 * Creates a small rounded marker icon displaying a ToDo title.
 *
 * @param title the title of the toDo to render inside the marker icon.
 * @return A [BitmapDescriptor] representing the ToDo marker icon.
 */
@Composable
private fun todoIcon(title: String): BitmapDescriptor {
  val density = LocalDensity.current
  val primary = MaterialTheme.colorScheme.primary
  val onPrimary = MaterialTheme.colorScheme.onPrimary
  return remember(title) {
    // Text Style
    val textPaint =
        Paint().apply {
          color = onPrimary.toArgb()
          textSize = with(density) { 20.sp.toPx() }
        }

    // measures
    val bounds = Rect().also { textPaint.getTextBounds(title, 0, title.length, it) }
    val hPad = with(density) { 8f * density.density }
    val vPad = with(density) { 4f * density.density }
    val w = (bounds.width() + 2 * hPad).toInt().coerceAtLeast(1)
    val h = (bounds.height() + 2 * vPad).toInt().coerceAtLeast(1)

    // creates the bitmap and canvas to draw on
    val bmp = createBitmap(w, h)
    val c = Canvas(bmp)

    // Box
    val bg = Paint().apply { color = primary.toArgb() }
    c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 12f, 12f, bg)

    // Text
    val baselineY = h / 2f + bounds.height() / 2f - bounds.bottom
    c.drawText(title, hPad, baselineY, textPaint)

    // creates the icon
    BitmapDescriptorFactory.fromBitmap(bmp)
  }
}

/**
 * Creates an expanded marker icon displaying a ToDo’s title, description, and due date.
 *
 * @param title The title of the ToDo displayed as the first line.
 * @param description The ToDo description displayed below the title.
 * @param dateText The formatted due date text to display below the title.
 * @param expanded Whether the icon represents an expanded state (affects recomposition key).
 * @return A [BitmapDescriptor] representing the expanded ToDo marker icon.
 */
@Composable
private fun todoExpanded(
    title: String,
    description: String,
    dateText: String,
    expanded: Boolean = false
): BitmapDescriptor {
  val density = LocalDensity.current
  val primary = MaterialTheme.colorScheme.primary
  val onPrimary = MaterialTheme.colorScheme.onPrimary
  return remember(title, description, dateText, expanded) {

    // Title style
    val titlePaint =
        Paint().apply {
          color = onPrimary.toArgb()
          textSize = with(density) { 20.sp.toPx() }
        }
    // Body Style
    val bodyPaint =
        Paint().apply {
          color = onPrimary.toArgb()
          textSize = with(density) { 15.sp.toPx() }
        }

    // Lines to draw
    val lines = buildList {
      add(title)
      add(dateText)
      add(description)
    }

    // Measure
    val tmp = Rect()
    var maxW = 0f
    var totalH = 0f
    lines.forEachIndexed { i, s ->
      val p = if (i == 0) titlePaint else bodyPaint
      p.getTextBounds(s, 0, s.length, tmp)
      maxW = maxOf(maxW, tmp.width().toFloat())
      totalH += tmp.height()
    }

    // Box metrics
    val hPad = with(density) { 20.dp.toPx() }
    val vPad = with(density) { 12.dp.toPx() }
    val corner = with(density) { 10.dp.toPx() }
    val lineGap = with(density) { 8.dp.toPx() }

    val width = (maxW + 2 * hPad).toInt().coerceAtLeast(1)
    val height = (totalH + 2 * vPad + ((lines.size - 1) * lineGap)).toInt().coerceAtLeast(1)

    // Bitmap & canvas
    val bmp = createBitmap(width, height)
    val c = Canvas(bmp)

    // Box
    val bg = Paint().apply { color = primary.toArgb() }
    c.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), corner, corner, bg)

    // Draw text lines
    var y = vPad
    lines.forEachIndexed { i, s ->
      val p = if (i == 0) titlePaint else bodyPaint
      p.getTextBounds(s, 0, s.length, tmp)
      val baseline = y + tmp.height() - tmp.bottom
      c.drawText(s, hPad, baseline, p)
      y = baseline + lineGap
    }

    // Create the icon
    BitmapDescriptorFactory.fromBitmap(bmp)
  }
}
