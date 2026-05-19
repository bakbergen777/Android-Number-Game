package com.example.adroid_lab_num_game

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NumberGameApp() }
    }

    // 🔊 Start / pause / stop background music with Activity lifecycle
    override fun onStart() {
        super.onStart()
        BgMusic.start(this)      // start/resume when app comes to foreground
    }

    override fun onStop() {
        super.onStop()
        BgMusic.pause()          // pause when app goes to background
    }

    override fun onDestroy() {
        super.onDestroy()
        BgMusic.stop()           // release when activity is destroyed
    }
}

@Composable
fun NumberGameApp() {
    MaterialTheme { Surface(Modifier.fillMaxSize()) { GameScreen() } }
}

@Composable
fun GameScreen() {
    var target by remember { mutableStateOf(Random.nextInt(1, 101)) }
    var guessText by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("Guess a number between 1 and 100") }
    var attempts by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }
    var running by remember { mutableStateOf(true) }

    // key to restart the timer when starting a new round
    var timerKey by remember { mutableStateOf(0) }
    val focus = LocalFocusManager.current

    // 60s round timer that restarts when timerKey changes
    DisposableEffect(timerKey, running) {
        val timer = if (running) object : CountDownTimer(60_000, 1_000) {
            override fun onTick(ms: Long) { timeLeft = (ms / 1000).toInt() }
            override fun onFinish() {
                running = false
                feedback = "⏰ Time’s up! It was $target."
            }
        }.start() else null
        onDispose { timer?.cancel() }
    }

    fun resetRound() {
        target = Random.nextInt(1, 101)
        attempts = 0
        guessText = ""
        feedback = "New round! Guess 1–100"
        timeLeft = 60
        running = true
        timerKey++   // triggers a fresh timer
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Number Game", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AssistChip(onClick = {}, label = { Text("Score: $score") })
            AssistChip(onClick = {}, label = { Text("Attempts: $attempts") })
            AssistChip(onClick = {}, label = { Text("Time: $timeLeft s") })
        }

        OutlinedTextField(
            value = guessText,
            onValueChange = { if (it.length <= 3) guessText = it.filter(Char::isDigit) },
            label = { Text("Your guess") },
            singleLine = true,
            enabled = running,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(enabled = running, onClick = {
                val guess = guessText.toIntOrNull()
                if (guess == null || guess !in 1..100) {
                    feedback = "Enter a number 1–100."
                    return@Button
                }
                attempts++
                when {
                    guess < target -> feedback = "Too low 🔽"
                    guess > target -> feedback = "Too high 🔼"
                    else -> {
                        feedback = "🎉 Correct! It was $target."
                        score += (100 - (attempts - 1) * 10).coerceAtLeast(10)
                        running = false
                    }
                }
            }) { Text("Guess") }

            OutlinedButton(onClick = {
                val hint = if (target % 2 == 0) "even" else "odd"
                feedback = "Hint: the number is $hint"
            }) { Text("Hint") }

            OutlinedButton(onClick = { score = 0; resetRound() }) { Text("Reset Game") }
            Button(onClick = { resetRound() }) { Text("Next Round") }
        }

        Text(
            feedback,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}