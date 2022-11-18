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

    fun connectSocket(): Boolean{
        try {
            mSocket = IO.socket("http://172.16.10.232:9999")
            mSocket.connect()
            Log.d("Connected", "OK")
            mSocket.on(Socket.EVENT_CONNECT, onConnect)
            return true
        } catch (e: URISyntaxException) {
            Log.d("ERR", e.toString())
        }
        return false
    }

    val onConnect = Emitter.Listener {
        mSocket.emit("emitReceive", "OK")
    }
}