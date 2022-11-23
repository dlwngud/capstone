package com.dlwngud.socket

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import com.dlwngud.socket.databinding.ActivityMainBinding
import com.dlwngud.socket.fragment.CallFragment
import com.dlwngud.socket.fragment.HomeFragment
import com.dlwngud.socket.fragment.ParkingFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.Constants.MessageNotificationKeys.TAG
import com.google.firebase.messaging.FirebaseMessaging
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException
import kotlin.concurrent.thread
import android.content.pm.PackageManager

import android.content.pm.PackageInfo
import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toDoSplash()

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // Check if the initial data is ready.
                    return if (isReady) {
                        // The content is ready; start drawing.
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        // The content is not ready; suspend.
                        false
                    }
                }
            }
        )

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    val homeFragment = HomeFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentFrame, homeFragment).commit()
                    true
                }
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

    private fun toDoSplash() {
        thread(start = true) {
            for (i in 1..5) {
                Thread.sleep(500)
                if (!isNetworkAvailable()) {
                    runOnUiThread {
                        Toast.makeText(this, "네트워크 상태를 확인해주세요.", Toast.LENGTH_SHORT).show()
                    }
                    break
                } else if (isNetworkAvailable() && com.dlwngud.socket.socket.Socket.connectSocket()) {
                    isReady = true
                    break
                }
            }
        }
    }

    fun getFirebaseToken() {
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
        })
    }

    // 앱의 해쉬값 확인
    private fun getHashKey() {
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        if (packageInfo == null) Log.e("KeyHash", "KeyHash:null")
        for (signature in packageInfo!!.signatures) {
            try {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            } catch (e: NoSuchAlgorithmException) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=$signature", e)
            }
        }
    }

    private val onMessage = Emitter.Listener { args ->
        val data = args[0].toString()
        Log.d("parking", data)

        runOnUiThread {
//            binding.tv.text = data
//            Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
        }
    }

    // 네트워크 연결 상태 확인
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = baseContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val info = connectivityManager.allNetworkInfo
            if (info != null) for (i in info.indices) if (info[i].state == NetworkInfo.State.CONNECTED) {
                Log.d("networkConnect", "OK")
                return true
            }
        }
        return false
    }
}