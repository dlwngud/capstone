package com.dlwngud.socket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dlwngud.socket.databinding.ActivityMainBinding
import com.dlwngud.socket.fragment.CallFragment
import com.dlwngud.socket.fragment.ParkingFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG
import com.google.firebase.messaging.FirebaseMessaging
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // 소켓 통신을 위한 Socket 객체 생성
//    lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 파이어베이스에서 토큰 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

             // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = token.toString();
            Log.d(TAG, msg)
//            binding.tv.text = msg
//            mSocket.emit("token", msg)
        })

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_parking -> {
                    val parkingFragment = ParkingFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentFrame, parkingFragment).commit()
                    true
                }
                R.id.action_call -> {
                    val callFragment = CallFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentFrame, callFragment).commit()
                    true
                }
                else -> false
            }
        }

        // 주차완료 메세지가 들어올때
//        mSocket.on("parking", onMessage)
    }


    private val onMessage = Emitter.Listener { args ->
        val data = args[0].toString()
        Log.d("parking", data)

        runOnUiThread {
//            binding.tv.text = data
//            Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
        }
    }
}