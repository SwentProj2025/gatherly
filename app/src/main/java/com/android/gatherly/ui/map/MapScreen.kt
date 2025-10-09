package com.android.gatherly.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

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
                    Marker(
                        state =
                            MarkerState(
                                position = LatLng(todo.location!!.latitude, todo.location.longitude)),
                        title = todo.name,
                        snippet = todo.description,
                        )
                }
            }
        })
}