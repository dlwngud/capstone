package com.dlwngud.socket.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dlwngud.socket.databinding.FragmentCallBinding
import com.dlwngud.socket.socket.Socket.mSocket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var mBinding: FragmentCallBinding? = null
private val binding get() = mBinding!!

private val dialogFragment = ProgressDialogFragment2()

class CallFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentCallBinding.inflate(inflater, container, false)

        // 호출을 알림
        binding.btnCall.setOnClickListener {
            mSocket.emit("call", "call")
            dialogFragment.show(childFragmentManager, " ")
        }

        mSocket.on("callback", onMessage)

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }

    private val onMessage = Emitter.Listener { args ->
        val data = args[0].toString()
        Log.d("callback", data)
        dialogFragment.dismiss()
    }
}