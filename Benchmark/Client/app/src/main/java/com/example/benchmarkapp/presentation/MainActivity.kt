/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.benchmarkapp.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.benchmarkapp.R
import com.example.benchmarkapp.presentation.core.GestureDataReader
import com.example.benchmarkapp.presentation.core.GestureDetector
import com.example.benchmarkapp.presentation.core.TimeRecorder
import com.example.benchmarkapp.presentation.theme.BenchmarkAppTheme
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearApp({ startBenchmark(this) })
        }
    }

    fun startBenchmark(context: Context){
        val gestureDataReader = GestureDataReader()
        val gestureDetector = GestureDetector(context)
        val timeRecorder = TimeRecorder()

        var filenames = arrayOf("Eat_1","Drink_1","Help_1","No_2","Toilet_1","Yes_1")
        for (filename in filenames){
            // read data
            gestureDataReader.readGestureData(context, filename)
            val gestureData = gestureDataReader.data

            // feed data and run model
            timeRecorder.startTimer()
            val detection = gestureDetector.detect(gestureData)
            var inferenceTime = timeRecorder.stopTimer()

            // prediction
            Log.i("File Name", filename)
            Log.i("Inference Time", inferenceTime.toString())
            Log.i("Prediction", detection.toString())

            gestureDataReader.reset()
        }
    }
}

@Composable
fun WearApp(onStart: () -> Unit) {
    val IDLE_STATE = "IDLE"
    val RUNNING_MODEL_STATE = "RUNNING"
    val currentState = remember { mutableStateOf(IDLE_STATE) }

    var prompt: String by remember{ mutableStateOf("${IDLE_STATE}") }

    BenchmarkAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                enabled = currentState.value == IDLE_STATE,
                onClick = {
                    currentState.value = RUNNING_MODEL_STATE
                    prompt = RUNNING_MODEL_STATE
                    onStart()
                    currentState.value = IDLE_STATE
                    prompt = IDLE_STATE
                }
            ) {
                Text(text = "Start")
            }
            Text(
                text = "$prompt"
            )
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp({})
}