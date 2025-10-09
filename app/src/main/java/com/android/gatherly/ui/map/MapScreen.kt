package com.android.gatherly.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.graphics.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.createBitmap

// Portions of the code in this file are copy-pasted from the Bootcamp solution provided by the SwEnt staff.
// The icons were created with the help of an LLM (ChatGPT).

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        content = { pd ->
            // Camera position state, using the first ToDo location if available
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(uiState.cameraPos, 10f)
            }
            GoogleMap(
                modifier =
                    Modifier.fillMaxSize().padding(pd),
                cameraPositionState = cameraPositionState) {
                uiState.todoList.forEach { todo ->
                    val loc = todo.location ?: return@forEach
                    val isExpanded = uiState.expandedTodoId == todo.uid

                    val formattedDate =
                        remember(todo.dueDate) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            sdf.format(todo.dueDate.toDate())
                        }

                    //not clicked
                    val iconCollapsed = todoIcon(todo.name)

                    //clicked
                    val iconExpanded = todoExpanded(
                        title = todo.name,
                        description = todo.description,
                        dateText = formattedDate,
                        expanded = true
                    )

                    Marker(
                        state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                        icon = if (isExpanded) iconExpanded else iconCollapsed,
                        onClick = {
                            if (isExpanded) viewModel.onTodoMarkerDismissed()
                            else viewModel.onTodoMarkerTapped(todo.uid)
                            true
                        }
                    )
                }
            }
        })
}

//Simple icon with the title
@Composable
private fun todoIcon(title: String): BitmapDescriptor {
    val density = LocalDensity.current
    return remember(title) {
        //Text Style
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = with(density) { 20.sp.toPx() }
        }

        //measures
        val bounds = Rect().also { textPaint.getTextBounds(title, 0, title.length, it) }
        val hPad = with(density) { 8f * density.density }
        val vPad = with(density) { 4f * density.density }
        val w = (bounds.width() + 2 * hPad).toInt().coerceAtLeast(1)
        val h = (bounds.height() + 2 * vPad).toInt().coerceAtLeast(1)

        //creates the bitmap and canvas to draw on
        val bmp = createBitmap(w, h)
        val c = Canvas(bmp)

        //Box
        val bg = Paint().apply { color = Color.BLUE }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), 12f, 12f, bg)

        //Text
        val baselineY = h / 2f + bounds.height() / 2f - bounds.bottom
        c.drawText(title, hPad, baselineY, textPaint)

        //creates the icon
        BitmapDescriptorFactory.fromBitmap(bmp)
    }
}

//Expanded icon with title, date, description
@Composable
fun todoExpanded(
    title: String,
    description: String,
    dateText: String,
    expanded: Boolean = false
): BitmapDescriptor {
    val density = LocalDensity.current
    return remember(title, description, dateText, expanded) {

        //Title style
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = with(density) { 20.sp.toPx() }
        }
        //Body Style
        val bodyPaint = Paint().apply {
            color = Color.WHITE
            textSize = with(density) { 15.sp.toPx() }
        }

        //Lines to draw
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
        val bg = Paint().apply { color = Color.BLUE }
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
