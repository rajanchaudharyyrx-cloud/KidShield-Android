package com.kidshield.agent.data.remote.websocket

import android.util.Log
import com.google.gson.Gson
import com.kidshield.agent.data.remote.api.DeviceCommand
import com.kidshield.agent.utils.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val gson: Gson
) {
    private var webSocket: WebSocketClient? = null
    private val _commands = MutableSharedFlow<DeviceCommand>(extraBufferCapacity = 100)
    val commands: SharedFlow<DeviceCommand> = _commands

    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState

    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun connect(authToken: String, deviceId: String) {
        disconnect()
        val uri = URI("${Constants.WS_URL}?token=$authToken&deviceId=$deviceId")
        webSocket = object : WebSocketClient(uri) {
            override fun onOpen(handshake: ServerHandshake?) {
                Log.d("WebSocket", "Connected")
                _connectionState.tryEmit(true)
                reconnectJob?.cancel()
            }

            override fun onMessage(message: String?) {
                message?.let {
                    try {
                        val command = gson.fromJson(it, DeviceCommand::class.java)
                        _commands.tryEmit(command)
                    } catch (e: Exception) {
                        Log.e("WebSocket", "Parse error: ${e.message}")
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("WebSocket", "Closed: $reason")
                _connectionState.tryEmit(false)
                scheduleReconnect(authToken, deviceId)
            }

            override fun onError(ex: Exception?) {
                Log.e("WebSocket", "Error: ${ex?.message}")
                _connectionState.tryEmit(false)
            }
        }
        webSocket?.connect()
    }

    fun disconnect() {
        reconnectJob?.cancel()
        webSocket?.close()
        webSocket = null
    }

    fun send(message: String): Boolean {
        return webSocket?.let {
            if (it.isOpen) {
                it.send(message)
                true
            } else false
        } ?: false
    }

    private fun scheduleReconnect(authToken: String, deviceId: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(5000)
            connect(authToken, deviceId)
        }
    }

    fun isConnected(): Boolean = webSocket?.isOpen == true
}
