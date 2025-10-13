package com.android.gatherly.ui.focusTimer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.gatherly.R
import com.android.gatherly.ui.theme.GatherlyTheme

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {

    val configuration = LocalConfiguration.current
    val corner = 12.dp

    Box(modifier = Modifier.fillMaxSize()) {}
    if (!viewModel.uiState.isStarted) {
        val timeWeight = 3f
        val buttonsWeight = 1f
        val todosWeight = 2f

        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().weight(timeWeight),
                verticalAlignment = Alignment.Bottom
            ) {
                TimerTime("00",
                    {},
                    stringResource(R.string.timer_hours),
                    corner)
                TimerTime("00",
                    {},
                    stringResource(R.string.timer_minutes),
                    corner)
                TimerTime("00",
                    {},
                    stringResource(R.string.timer_seconds),
                    corner)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().weight(buttonsWeight)
            ) {
                TimerButton({},
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    stringResource(R.string.timer_start),
                    corner)
                TimerButton({},
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    stringResource(R.string.timer_reset),
                    corner)
            }
            // Todos to link
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(todosWeight)
            ) {
                val padding = 6.dp
                val todoHeight = 60.dp
                val horizontalThickness = 0.5.dp

                item {
                    Text(
                        text = stringResource(R.string.timer_todos_linking),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(padding)
                    )
                }
                for (todo in viewModel.uiState.todos) {
                    item {
                        HorizontalDivider(
                            thickness = horizontalThickness,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Card(
                            colors =
                                if (todo == viewModel.uiState.linkedTodo) {
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    )
                                } else {
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        contentColor = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                            shape = RectangleShape,
                            modifier = Modifier.fillMaxWidth().height(todoHeight).clickable(onClick = {})
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = padding)
                            ) {
                                Text(
                                    text = todo.name,
                                    modifier = Modifier.padding(padding).align(Alignment.CenterStart)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Second view, timer is running or paused
    if(viewModel.uiState.isStarted) {
        val todoWeight = 1f
        val timerWeight = 2f
        val buttonsWeight = 1f
        
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            // Linked todo
            Box(
                modifier = Modifier.weight(todoWeight),
                contentAlignment = Alignment.BottomCenter
            ) {
                val todoRatio = 2.0/3.0

                if (viewModel.uiState.linkedTodo != null) {
                    Text ( text =
                        buildAnnotatedString {
                            append(stringResource(R.string.timer_linked_todo))
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(viewModel.uiState.linkedTodo?.name ?: "")
                            }
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width((configuration.screenWidthDp * todoRatio).dp)
                    )
                }
            }

            // Timer
            Box(
                modifier = Modifier.weight(timerWeight),
                contentAlignment = Alignment.Center
            ) {
                val progressRatio = 6.0/7.0
                val progressWidth = 15.dp
                val progressGap = 5.dp

                Box(
                    modifier = Modifier.height((configuration.screenWidthDp * progressRatio).dp)
                        .width((configuration.screenWidthDp * progressRatio).dp)
                ) {
                    // Circular time left
                    CircularProgressIndicator(
                        progress = { 0.87f },
                        strokeWidth = progressWidth,
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        gapSize = progressGap,
                        modifier = Modifier.width((configuration.screenWidthDp * progressRatio).dp)
                    )

                    // Time left
                    Text(
                        text = "00:20:03",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Control buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().weight(buttonsWeight)
            ) {
                TimerButton({},
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.onSecondary,
                    if(!viewModel.uiState.isPaused) {
                        stringResource(R.string.timer_pause)
                    } else {
                        stringResource(R.string.timer_resume)
                    },
                    corner)
                TimerButton({},
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    stringResource(R.string.timer_stop),
                    corner)
            }
        }
    }
}

@Composable
fun TimerButton(onClick : () -> Unit, containerColor : Color, contentColor : Color, text : String, corner : Dp) {

    val configuration = LocalConfiguration.current
    val buttonRatio = 3.0/7.0

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier.width((configuration.screenWidthDp * buttonRatio).dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TimerTime(time : String, onValueChange: (String) -> Unit, text : String, corner : Dp) {
    val configuration = LocalConfiguration.current
    val timeRatio = 1.0/4.0
    val timeFontSize = 25.sp

    Column(
        modifier = Modifier.width((configuration.screenWidthDp * timeRatio).dp)
    ) {
        TextField(
            value = time,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(corner),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = timeFontSize
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun TimerScreenPreview() {
    GatherlyTheme { TimerScreen() }
}