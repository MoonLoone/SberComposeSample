package com.example.sbercomposesample

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainScreenState(
    val loading: Boolean,
    val error: Boolean,
    val data: List<String>,
)

class MainScreenViewModel : ViewModel() {
    private val repository: MainScreenRepository = MainScreenRepository()

    val state: MutableStateFlow<MainScreenState> = MutableStateFlow(
        MainScreenState(
            loading = true,
            error = false,
            data = emptyList(),
        )
    )

    init {
        loadRemoteData(querySearch = null)
    }

    fun loadData(querySearch: String?) {
        state.value = MainScreenState(
            loading = true,
            error = false,
            data = emptyList(),
        )
        loadRemoteData(querySearch = querySearch)
    }

    private fun loadRemoteData(querySearch: String?) {
        viewModelScope.launch {
            repository.loadData(querySearch.orEmpty()).getOrNull()?.let {
                state.value = MainScreenState(
                    loading = false,
                    error = false,
                    data = it,
                )
            }
        }
    }

}

@SuppressLint("FrequentlyChangedStateReadInComposition")
@Composable
fun MainScreen(
    state: MainScreenState,
    loadData: (String) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    var textFieldValue = ""
    val firstElementVisible = remember(scrollState.firstVisibleItemIndex) {
        scrollState.firstVisibleItemIndex != 0
    }

    LaunchedEffect(firstElementVisible) {
        if (firstElementVisible) {
            Toast.makeText(context, "Вернись к поиску", Toast.LENGTH_SHORT).show()
        }
    }

    when {
        state.loading -> {
            RotatingCircleLoader(modifier = Modifier.fillMaxSize())
        }

        state.error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Ошибка при загрузке")
            }
        }

        else -> {
            ShakeDetector(
                onShake = {
                    loadData(textFieldValue)
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                    )

                    LazyColumn(
                        modifier = Modifier.graphicsLayer {
                            translationY = 13.dp.toPx()
                        },
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Image(
                                    painter = painterResource(R.drawable.ic_launcher_background),
                                    contentDescription = null,
                                )
                                Text(
                                    it,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                Button(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = {
                        loadData(textFieldValue)
                    }
                ) {
                    Text("Поиск")
                }
            }
        }
    }
}


@Composable
fun RotatingCircleLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedFloat = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
    )

    Box(
        modifier = modifier.rotate(animatedFloat.value),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = modifier
        ) {
            val canvasWidth = size.width / 2
            val canvasHeight = size.height / 2

            drawArc(
                color = Color.Blue,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(0f, canvasHeight / 2),
                size = Size(canvasWidth, canvasHeight),
            )
        }
    }
}

@Composable
private fun ShakeDetector(
    onShake: () -> Unit,
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val shakeDetector = createSensorListener(onShake)
    sensorManager.registerListener(
        shakeDetector,
        accelerometerSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    DisposableEffect(context) {
        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }
}