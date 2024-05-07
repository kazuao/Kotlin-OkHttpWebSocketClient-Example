package com.example.okhttpwebsocketclientexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.okhttpwebsocketclientexample.ui.theme.OkHttpWebSocketClientExampleTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private lateinit var webSocketClient: WebSocketClient
    private val socketListener = object : WebSocketClient.SocketListener {
        override fun onMessage(message: String) {
            Timber.d("Receive message $message")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        webSocketClient = WebSocketClient.getInstance()
        webSocketClient.setSocketUtl("ws://192.168.50.89:5001")
        webSocketClient.setListener(socketListener)

        setContent {
            OkHttpWebSocketClientExampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { i ->
                    MainScreen(
                        onConnect = {
                            webSocketClient.connect()
                        },
                        onSendMessage = {
                            webSocketClient.sendMessage(it)
                        },
                        onDisconnect = {
                            webSocketClient.disconnect()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    onSendMessage: (String) -> Unit,
    onDisconnect: () -> Unit,
    onConnect: () -> Unit,
) {
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Socket Demo", modifier = Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = message,
                onValueChange = {
                    message = it
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                val m = """
                    {
                        "id": 1,
                        "device_id": 123,
                        "session_id": "session_id",
                        "timestamp": "2021-11-11 11:11:11.11",
                        "utilization_status": 1,
                        "cmd": "subsc.utterance",
                        "arg": null
                    }
                """.trimIndent()
                onSendMessage(m)
                message = ""
            }) {
                Text(text = "Send Message")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onConnect() }) {
                Text(text = "Connect Socket")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = { onDisconnect() }) {
                Text(text = "Disconnect Socket")
            }
        }

    }
}