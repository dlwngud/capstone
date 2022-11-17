package com.dlwngud.socket.fragment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dlwngud.socket.R
import com.dlwngud.socket.databinding.FragmentParkingBinding
import com.dlwngud.socket.socket.Socket.mSocket
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

// 바인딩 객체 타입에 ?를 붙여서 null을 허용 해줘야한다. ( onDestroy 될 때 완벽하게 제거를 하기위해 )
private var mBinding: FragmentParkingBinding? = null

// 매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
private val binding get() = mBinding!!

private val dialogFragment = ProgressDialogFragment()

class ParkingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentParkingBinding.inflate(inflater, container, false)

        // 주행 시작을 알림
        binding.btnParking.setOnClickListener {
            mSocket.emit("drive", "drive")
            dialogFragment.show(childFragmentManager, " ")
        }

        mSocket.on("parking", onMessage)

        return binding.root
    }

    // 프래그먼트가 destroy 될때
    override fun onDestroyView() {
        // onDestroyView 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroyView()
    }

    private val onMessage = Emitter.Listener { args ->
        val data = args[0].toString()
        Log.d("parking", data)
        dialogFragment.dismiss()
    }
}