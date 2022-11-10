package com.dlwngud.socket.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dlwngud.socket.R
import com.dlwngud.socket.databinding.FragmentCallBinding

private var mBinding: FragmentCallBinding? = null
private val binding get() = mBinding!!

class CallFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentCallBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        mBinding = null
        super.onDestroyView()
    }
}