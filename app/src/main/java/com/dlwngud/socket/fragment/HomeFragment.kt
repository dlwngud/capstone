package com.dlwngud.socket.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dlwngud.socket.databinding.FragmentHomeBinding

private var mBinding: FragmentHomeBinding? = null
private val binding get() = mBinding!!

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    // 프래그먼트가 destroy 될때
    override fun onDestroyView() {
        // onDestroyView 에서 binding class 인스턴스 참조를 정리해주어야 한다.
        mBinding = null
        super.onDestroyView()
    }
}