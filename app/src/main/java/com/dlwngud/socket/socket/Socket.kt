package com.dlwngud.socket.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

// .on = 메시지를 받았을 때 동작 수행.
// .emit = 메시지를 전송하는 동작 수행.

object Socket {
    // 소켓 통신을 위한 Socket 객체 생성
    lateinit var mSocket: Socket

    fun connectSocket(){
        try {
            mSocket = IO.socket("http://192.168.0.5:9999")
            mSocket.connect()
            Log.d("Connected", "OK")
        } catch (e: URISyntaxException) {
            Log.d("ERR", e.toString())
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect)
    }

    val onConnect = Emitter.Listener {
        mSocket.emit("emitReceive", "OK")
    }
}