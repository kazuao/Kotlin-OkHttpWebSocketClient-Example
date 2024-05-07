package com.example.okhttpwebsocketclientexample

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import timber.log.Timber

class WebSocketClient {
    interface SocketListener {
        fun onMessage(message: String)
    }

    private lateinit var webSocket: WebSocket
    private var socketListener: SocketListener? = null
    private var socketUrl = ""
    private var shouldReconnect = true
    private var client: OkHttpClient? = null

    companion object {
        private lateinit var instance: WebSocketClient
        @Synchronized
        fun getInstance(): WebSocketClient {
            synchronized(WebSocketClient::class) {
                if (!::instance.isInitialized) {
                    instance = WebSocketClient()
                }
            }
            return instance
        }
    }

    fun setListener(listener: SocketListener) {
        this.socketListener = listener
    }

    fun setSocketUtl(url: String) {
        socketUrl = url
    }

    private fun initWebSocket() {
        Timber.d("init websocket")
        client = OkHttpClient()

        val request = Request.Builder()
            .url(url = socketUrl)
            .build()
        webSocket = client!!.newWebSocket(request, webSocketListener)
        client!!.dispatcher.executorService.shutdown()
    }

    fun connect() {
        Timber.d("connect")
        shouldReconnect = true
        initWebSocket()
    }

    fun reconnect() {
        Timber.d("reconnect")
        initWebSocket()
    }

    fun sendMessage(message: String) {
        Timber.d("sendMessage $message")
        if (::webSocket.isInitialized)
            webSocket.send(message)
    }

    fun disconnect() {
        if (::webSocket.isInitialized)
            webSocket.close(1000, "Do not need connection anymore.")
        shouldReconnect = false
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            Timber.d("WebSocketListener: onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            Timber.d("WebSocketListener: onMessage = $text")
            socketListener?.onMessage(text)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            Timber.d("WebSocketListener: onClosing")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            Timber.d("WebSocketListener: onClosed")
            if (shouldReconnect)
                reconnect()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            Timber.d("WebSocketListener: onFailure")
            if (shouldReconnect)
                reconnect()
        }
    }
}