package com.dlwngud.socket

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.net.NetworkInfo
import android.widget.Toast


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (isNetworkAvailable()) {
            splash()
        } else {
            Toast.makeText(this,"네트워크 상태를 확인해주세요.",Toast.LENGTH_SHORT).show()
        }

    }

    private fun splash() {
        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1000
        )
    }


    // 네트워크 연결 상태 확인
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = baseContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val info = connectivityManager.allNetworkInfo
            if (info != null) for (i in info.indices) if (info[i].state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}